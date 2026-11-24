package com.sarthi.service.Impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.sarthi.dto.StoreCorrectionSlipRequestDTO;
import com.sarthi.dto.StoreCorrectionSlipResponseDTO;
import com.sarthi.entity.certificate.CertificateStorage;
import com.sarthi.entity.certificate.CorrectionSlipDocument;
import com.sarthi.repository.certificate.CertificateStorageRepository;
import com.sarthi.repository.certificate.CorrectionSlipDocumentRepository;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.service.CaseLetterService;
import com.sarthi.service.CorrectionSlipStorageService;
import com.sarthi.service.PdfCompressionService;
import com.sarthi.util.BlobFolderResolver;
import com.sarthi.util.FileCompressionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CorrectionSlipStorageServiceImpl implements CorrectionSlipStorageService {

    private static final String STAGE_PRE_SIGN = "PRE_SIGN";
    private static final String STATUS_ACTIVE = "ACTIVE";

    private final CorrectionSlipDocumentRepository documentRepository;
    private final PdfCompressionService pdfCompressionService;
    private final CertificateStorageRepository certificateStorageRepository;
    private final AzureBlobStorageService azureBlobStorageService;
    private final CaseLetterService caseLetterService;
    private final JdbcTemplate jdbcTemplate;

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.correctionslip-container-name:ic-correctionslip}")
    private String correctionSlipContainerName;

    @Value("${azure.storage.uat-connection-string:}")
    private String uatConnectionString;

    @Value("${azure.storage.prod-connection-string:}")
    private String prodConnectionString;

    private volatile BlobContainerClient containerClient;

    private BlobContainerClient getContainerClient() {
        if (containerClient == null) {
            synchronized (this) {
                if (containerClient == null) {
                    try {
                        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                                .connectionString(connectionString)
                                .buildClient();
                        BlobContainerClient client = blobServiceClient.getBlobContainerClient(correctionSlipContainerName);
                        try {
                            client.createIfNotExists();
                        } catch (Exception e) {
                            log.debug("Container already exists or verified: {}", e.getMessage());
                        }
                        containerClient = client;
                    } catch (Exception e) {
                        log.warn("Could not initialize Azure BlobContainerClient for container '{}': {}", correctionSlipContainerName, e.getMessage());
                        return null;
                    }
                }
            }
        }
        return containerClient;
    }

    private boolean isLocalOrInvalidAzure() {
        if (connectionString == null || connectionString.trim().isEmpty()) return true;
        String trimmed = connectionString.trim().replace("\"", "").replace("'", "");
        return trimmed.equals("sdfghjk") || trimmed.length() < 25 || !trimmed.contains("DefaultEndpointsProtocol=");
    }

    @Override
    @Transactional
    public StoreCorrectionSlipResponseDTO compressAndStore(StoreCorrectionSlipRequestDTO request) {
        if (request == null || request.getCallNo() == null || request.getCallNo().trim().isEmpty()) {
            throw new IllegalArgumentException("Call Number is required");
        }
        if (request.getPdfBase64() == null || request.getPdfBase64().trim().isEmpty()) {
            throw new IllegalArgumentException("PDF base64 data is required");
        }

        String callNo = request.getCallNo().trim();
        String moduleType = (request.getModuleType() != null ? request.getModuleType().trim().toUpperCase() : "ERC");
        String stage = (request.getStage() != null ? request.getStage().trim().toUpperCase() : STAGE_PRE_SIGN);

        String rawBase64 = request.getPdfBase64();
        if (rawBase64.contains(",")) {
            rawBase64 = rawBase64.split(",")[1];
        }

        byte[] originalBytes;
        try {
            originalBytes = Base64.getDecoder().decode(rawBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encoded PDF string", e);
        }

        long originalSize = originalBytes.length;

        // 1. Compress the PDF (unless it is already digitally signed)
        byte[] processedBytes;
        if ("SIGNED".equalsIgnoreCase(stage)) {
            // Signed PDF: preserve exact signed bytes so signature remains cryptographically valid
            processedBytes = originalBytes;
        } else {
            // Pre-sign: compress PDF structure and streams for maximum optimization
            byte[] compressedPdf = pdfCompressionService.compressPdf(originalBytes);
            processedBytes = (compressedPdf.length < originalBytes.length) ? compressedPdf : originalBytes;
            log.info("Correction slip PDF compressed before signing for {}: {} -> {} bytes ({:.2f}%)",
                    callNo, originalSize, processedBytes.length, (processedBytes.length * 100.0 / originalSize));
        }

        long finalSize = processedBytes.length;
        String compressedBase64 = Base64.getEncoder().encodeToString(processedBytes);

        // 2. Resolve Azure folder hierarchy: erc/ER, erc/EP, erc/EF, railpad/RPP, railpad/RPF, sleeper
        String folderPrefix = BlobFolderResolver.resolveFolder(callNo, moduleType);

        // An unfinished pre-sign copy (eSign started but not completed, or retried) is reused instead of
        // inserting a new row, so one correction slip never appears as several.
        CorrectionSlipDocument reuseDoc = documentRepository
                .findFirstByCallNoAndStageAndStatusOrderByUploadedAtDesc(callNo, STAGE_PRE_SIGN, STATUS_ACTIVE)
                .filter(d -> d.getBlobFileName() != null && !d.getBlobFileName().trim().isEmpty())
                .orElse(null);

        String sanitizedFileName;
        String blobPath;
        if (reuseDoc != null) {
            blobPath = reuseDoc.getBlobFileName().trim();
            sanitizedFileName = reuseDoc.getOriginalFileName() != null
                    ? reuseDoc.getOriginalFileName()
                    : new File(blobPath).getName();
        } else {
            List<CorrectionSlipDocument> existingDocs = getAllDocuments(callNo);
            int nextIndex = existingDocs.size() + 1;

            String sanitizedCallNo = callNo.replaceAll("[^a-zA-Z0-9_-]", "_");
            sanitizedFileName = String.format("Correction_Slip_%s_%d.pdf", sanitizedCallNo, nextIndex);
            blobPath = String.format("%s/%s/%s", folderPrefix, sanitizedCallNo, sanitizedFileName);
        }

        // 3. Upload to Azure or local storage
        String blobUrl;
        if (isLocalOrInvalidAzure()) {
            log.info("Saving correction slip to local storage (dev mode): {}", blobPath);
            blobUrl = saveToLocal(blobPath, processedBytes);
        } else {
            try {
                BlobContainerClient containerClient = getContainerClient();
                BlobClient blobClient = containerClient.getBlobClient(blobPath);
                blobClient.upload(new ByteArrayInputStream(processedBytes), processedBytes.length, true);
                blobUrl = blobClient.getBlobUrl();
                log.info("Uploaded correction slip to Azure container '{}': {}", correctionSlipContainerName, blobUrl);
            } catch (Exception e) {
                log.error("Azure upload to container '{}' failed, falling back to local storage: {}",
                        correctionSlipContainerName, e.getMessage());
                blobUrl = saveToLocal(blobPath, processedBytes);
            }
        }

        // 4. Save the CorrectionSlipDocument row in DB (update the open pre-sign row, or insert a new one)
        String uploadedBy = request.getUploadedBy() != null ? request.getUploadedBy() : "Inspecting Engineer";
        CorrectionSlipDocument doc;
        if (reuseDoc != null) {
            doc = reuseDoc;
            doc.setIcNumber(request.getIcNumber());
            doc.setModuleType(moduleType);
            doc.setBlobUrl(blobUrl);
            doc.setFileSizeOriginal(originalSize);
            doc.setFileSizeCompressed(finalSize);
            doc.setStage(stage);
            doc.setUploadedBy(uploadedBy);
            doc.setUploadedAt(java.time.LocalDateTime.now());
        } else {
            doc = CorrectionSlipDocument.builder()
                    .callNo(callNo)
                    .status(STATUS_ACTIVE)
                    .icNumber(request.getIcNumber())
                    .moduleType(moduleType)
                    .originalFileName(sanitizedFileName)
                    .blobFileName(blobPath)
                    .blobUrl(blobUrl)
                    .fileSizeOriginal(originalSize)
                    .fileSizeCompressed(finalSize)
                    .contentType("application/pdf")
                    .stage(stage)
                    .uploadedBy(uploadedBy)
                    .build();
        }

        documentRepository.save(doc);

        return StoreCorrectionSlipResponseDTO.builder()
                .success(true)
                .message("Correction slip compressed and stored successfully in " + correctionSlipContainerName)
                .callNo(callNo)
                .icNumber(request.getIcNumber())
                .fileName(sanitizedFileName)
                .blobFileName(blobPath)
                .blobUrl(blobUrl)
                .compressedBase64(compressedBase64)
                .originalSize(originalSize)
                .compressedSize(finalSize)
                .stage(stage)
                .build();
    }

    private String saveToLocal(String relativePath, byte[] data) {
        try {
            File targetFile = new File("uploads/correction_slips", relativePath);
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(data);
            }
            return "/uploads/correction_slips/" + relativePath.replace('\\', '/');
        } catch (IOException e) {
            log.error("Error saving local correction slip: {}", e.getMessage(), e);
            return "local://" + relativePath;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CorrectionSlipDocument> getLatestDocument(String callNo) {
        if (callNo == null || callNo.trim().isEmpty()) {
            return Optional.empty();
        }
        String clean = callNo.replaceAll("^:+", "").trim();
        String alt = clean.contains(" ") ? clean.replace(" ", "-") : clean.replace("-", " ");
        // Same candidate keys and order as before; unfinished pre-sign copies are skipped.
        for (String candidate : List.of(clean, ":" + clean, alt, ":" + alt)) {
            Optional<CorrectionSlipDocument> docOpt = documentRepository
                    .findByCallNoAndStatusOrderByUploadedAtDesc(candidate, STATUS_ACTIVE)
                    .stream()
                    .filter(this::isIssued)
                    .findFirst();
            if (docOpt.isPresent()) {
                return docOpt;
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorrectionSlipDocument> getIssuedDocuments(String callNo) {
        List<CorrectionSlipDocument> all = getAllDocuments(callNo);
        List<CorrectionSlipDocument> issued = new ArrayList<>();
        for (CorrectionSlipDocument doc : all) {
            if (isIssued(doc)) {
                issued.add(doc);
            }
        }
        return issued;
    }

    /** A correction slip counts as issued unless it is only a pre-sign copy (null stage = legacy, issued). */
    private boolean isIssued(CorrectionSlipDocument doc) {
        return doc != null && (doc.getStage() == null || !STAGE_PRE_SIGN.equalsIgnoreCase(doc.getStage().trim()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorrectionSlipDocument> getAllDocuments(String callNo) {
        if (callNo == null || callNo.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String clean = callNo.replaceAll("^:+", "").trim();
        List<String> variations = new ArrayList<>();
        variations.add(clean);
        variations.add(":" + clean);
        if (clean.contains(" ")) {
            variations.add(clean.replace(" ", "-"));
            variations.add(clean.replace(" ", "/"));
            variations.add(clean.replace(" ", "_"));
            variations.add(":" + clean.replace(" ", "-"));
        }
        if (clean.contains("-")) {
            variations.add(clean.replace("-", " "));
            variations.add(clean.replace("-", "/"));
            variations.add(clean.replace("-", "_"));
            variations.add(":" + clean.replace("-", " "));
        }
        if (clean.contains("/")) {
            variations.add(clean.replace("/", "-"));
            variations.add(clean.replace("/", " "));
            variations.add(clean.replace("/", "_"));
        }

        List<CorrectionSlipDocument> list = documentRepository.findAllMatchingForCall(variations);
        if (list == null || list.isEmpty()) {
            list = documentRepository.findByCallNoInAndStatusOrderByUploadedAtAsc(variations, STATUS_ACTIVE);
        }
        return list != null ? list : Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> viewPdf(String callNo) {
        return getPdfResource(callNo, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadPdf(String callNo) {
        return getPdfResource(callNo, false);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> viewPdfById(Long id) {
        return getPdfResourceById(id, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadPdfById(Long id) {
        return getPdfResourceById(id, false);
    }

    private ResponseEntity<Resource> getPdfResource(String callNo, boolean inline) {
        if (callNo == null || callNo.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<CorrectionSlipDocument> optDoc = getLatestDocument(callNo);
        if (optDoc.isEmpty()) {
            log.warn("No correction slip document found in database for call: {}", callNo);
            return ResponseEntity.notFound().build();
        }

        return buildPdfResponse(optDoc.get(), inline);
    }

    private ResponseEntity<Resource> getPdfResourceById(Long id, boolean inline) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<CorrectionSlipDocument> optDoc = documentRepository.findByIdAndStatus(id, STATUS_ACTIVE);
        if (optDoc.isEmpty()) {
            log.warn("No correction slip document found in database for id: {}", id);
            return ResponseEntity.notFound().build();
        }

        return buildPdfResponse(optDoc.get(), inline);
    }

    @Override
    public byte[] getDocumentBytes(CorrectionSlipDocument doc) {
        if (doc == null) return null;
        log.info("getDocumentBytes for doc id={}, callNo='{}', blobFileName='{}', blobUrl='{}', defaultContainer='{}'",
                doc.getId(), doc.getCallNo(), doc.getBlobFileName(), doc.getBlobUrl(), correctionSlipContainerName);

        byte[] data = null;

        List<String> candidatePaths = new ArrayList<>();
        if (doc.getBlobFileName() != null && !doc.getBlobFileName().trim().isEmpty()) {
            candidatePaths.add(doc.getBlobFileName().trim());
            try {
                String decoded = java.net.URLDecoder.decode(doc.getBlobFileName().trim(), java.nio.charset.StandardCharsets.UTF_8);
                if (!candidatePaths.contains(decoded)) candidatePaths.add(decoded);
            } catch (Exception ignored) {}
        }

        String urlContainer = null;
        String urlBlobPath = null;
        String urlAccount = null;

        if (doc.getBlobUrl() != null && doc.getBlobUrl().trim().startsWith("http")) {
            try {
                java.net.URI uri = new java.net.URI(doc.getBlobUrl().trim());
                String host = uri.getHost();
                if (host != null && host.contains(".blob.core.windows.net")) {
                    urlAccount = host.substring(0, host.indexOf(".blob.core.windows.net"));
                }
                String path = uri.getPath();
                if (path != null) {
                    if (path.startsWith("/")) path = path.substring(1);
                    int firstSlash = path.indexOf('/');
                    if (firstSlash > 0) {
                        urlContainer = path.substring(0, firstSlash);
                        urlBlobPath = path.substring(firstSlash + 1);
                        if (urlBlobPath.contains("?")) {
                            urlBlobPath = urlBlobPath.substring(0, urlBlobPath.indexOf('?'));
                        }
                        if (!candidatePaths.contains(urlBlobPath)) candidatePaths.add(urlBlobPath);
                        try {
                            String decoded = java.net.URLDecoder.decode(urlBlobPath, java.nio.charset.StandardCharsets.UTF_8);
                            if (!candidatePaths.contains(decoded)) candidatePaths.add(decoded);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception e) {
                log.debug("Could not parse blobUrl: {}", e.getMessage());
            }
        }

        // 1. Try local file checks
        for (String path : candidatePaths) {
            List<File> localFiles = List.of(
                    new File("uploads/correction_slips", path),
                    new File("uploads", path),
                    new File("uploads/correction_slips", new File(path).getName())
            );
            for (File f : localFiles) {
                if (f.exists() && f.isFile()) {
                    try {
                        data = Files.readAllBytes(f.toPath());
                        log.info("Read correction slip from local file: {}", f.getPath());
                        break;
                    } catch (IOException e) {
                        log.warn("Could not read local file {}: {}", f.getPath(), e.getMessage());
                    }
                }
            }
            if (data != null) break;
        }

        // 2. Try Azure Blob Storage downloads with multi-account & multi-container fallback
        if (data == null) {
            List<String> connectionStringsToTry = new ArrayList<>();
            if (urlAccount != null && urlAccount.equalsIgnoreCase("ridesurestorageaccount")) {
                if (isValidAzure(uatConnectionString)) connectionStringsToTry.add(uatConnectionString);
                if (isValidAzure(connectionString) && !connectionStringsToTry.contains(connectionString)) connectionStringsToTry.add(connectionString);
                if (isValidAzure(prodConnectionString) && !connectionStringsToTry.contains(prodConnectionString)) connectionStringsToTry.add(prodConnectionString);
            } else if (urlAccount != null && urlAccount.equalsIgnoreCase("ritesblobstorage")) {
                if (isValidAzure(prodConnectionString)) connectionStringsToTry.add(prodConnectionString);
                if (isValidAzure(connectionString) && !connectionStringsToTry.contains(connectionString)) connectionStringsToTry.add(connectionString);
                if (isValidAzure(uatConnectionString) && !connectionStringsToTry.contains(uatConnectionString)) connectionStringsToTry.add(uatConnectionString);
            } else {
                if (isValidAzure(connectionString)) connectionStringsToTry.add(connectionString);
                if (isValidAzure(prodConnectionString) && !connectionStringsToTry.contains(prodConnectionString)) connectionStringsToTry.add(prodConnectionString);
                if (isValidAzure(uatConnectionString) && !connectionStringsToTry.contains(uatConnectionString)) connectionStringsToTry.add(uatConnectionString);
            }

            List<String> containersToTry = new ArrayList<>();
            if (urlContainer != null && !urlContainer.isBlank()) containersToTry.add(urlContainer);
            if (correctionSlipContainerName != null && !containersToTry.contains(correctionSlipContainerName)) containersToTry.add(correctionSlipContainerName);
            if (!containersToTry.contains("ic-correctionslip")) containersToTry.add("ic-correctionslip");
            if (!containersToTry.contains("sarthi-uploads")) containersToTry.add("sarthi-uploads");
            if (!containersToTry.contains("certificates")) containersToTry.add("certificates");

            for (String connStr : connectionStringsToTry) {
                for (String cName : containersToTry) {
                    for (String bPath : candidatePaths) {
                        data = tryDownloadFromAzure(connStr, cName, bPath);
                        if (data != null) {
                            try {
                                saveToLocal(bPath, data);
                            } catch (Exception ignored) {}
                            break;
                        }
                    }
                    if (data != null) break;
                }
                if (data != null) break;
            }
        }

        // 3. Fallback: try streaming directly from blob URL if available
        if (data == null && doc.getBlobUrl() != null && doc.getBlobUrl().startsWith("http")) {
            try {
                java.net.URL url = new java.net.URI(doc.getBlobUrl()).toURL();
                try (java.io.InputStream in = url.openStream()) {
                    data = in.readAllBytes();
                }
            } catch (Exception e) {
                log.debug("Direct HTTP stream from blobUrl '{}' failed (expected if container is private): {}", doc.getBlobUrl(), e.getMessage());
            }
        }

        if (data == null) {
            return null;
        }

        return FileCompressionUtil.decompressIfNeeded(data);
    }

    private ResponseEntity<Resource> buildPdfResponse(CorrectionSlipDocument doc, boolean inline) {
        byte[] decompressedBytes = getDocumentBytes(doc);
        if (decompressedBytes == null) {
            log.warn("Correction slip file content could not be retrieved from local or Azure for id: {}", doc.getId());
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(decompressedBytes);

        String disposition = inline ? "inline" : "attachment";
        String filename = doc.getOriginalFileName() != null ? doc.getOriginalFileName() : ("Correction_Slip_" + doc.getCallNo() + ".pdf");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + filename + "\"")
                .body(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getMergedIcAndCorrectionSlips(String callNo) {
        if (callNo == null || callNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Call number is required.");
        }
        String cleanCall = callNo.replaceAll("^:+", "").trim();

        // 1. Fetch all issued (non pre-sign) correction slip documents
        List<CorrectionSlipDocument> slipDocs = getIssuedDocuments(cleanCall);
        if (slipDocs == null || slipDocs.isEmpty()) {
            throw new NoSuchElementException("No correction slip has been found this call");
        }

        List<byte[]> pdfList = new ArrayList<>();

        // 2. Fetch original IC bytes
        byte[] icBytes = fetchOriginalIcBytes(cleanCall, slipDocs);
        if (icBytes != null && icBytes.length > 0) {
            log.info("Found original IC ({} bytes) for call: {}", icBytes.length, cleanCall);
            pdfList.add(icBytes);
        } else {
            log.warn("Original IC not found or could not be downloaded for call: {}", cleanCall);
        }

        // 3. Fetch each correction slip PDF bytes in sequence
        for (CorrectionSlipDocument doc : slipDocs) {
            byte[] slipBytes = getDocumentBytes(doc);
            if (slipBytes != null && slipBytes.length > 0) {
                log.info("Loaded correction slip id={} ({} bytes) for call: {}", doc.getId(), slipBytes.length, cleanCall);
                pdfList.add(slipBytes);
            } else {
                log.warn("Correction slip id={} could not be retrieved for call: {}", doc.getId(), cleanCall);
            }
        }

        if (pdfList.isEmpty()) {
            throw new RuntimeException("No valid PDF content found to merge for call: " + cleanCall);
        }

        if (pdfList.size() == 1) {
            return pdfList.get(0);
        }

        return caseLetterService.mergePdfs(pdfList);
    }

    private byte[] fetchOriginalIcBytes(String callNo, List<CorrectionSlipDocument> slipDocs) {
        List<String> candidates = new ArrayList<>();
        candidates.add(callNo);
        if (callNo.contains(" ")) candidates.add(callNo.replace(" ", "-"));
        if (callNo.contains("-")) candidates.add(callNo.replace("-", "/"));
        if (callNo.contains("/")) candidates.add(callNo.replace("/", "-"));

        for (CorrectionSlipDocument doc : slipDocs) {
            if (doc.getIcNumber() != null && !doc.getIcNumber().isBlank()) {
                String ic = doc.getIcNumber().trim();
                if (!candidates.contains(ic)) candidates.add(ic);
                if (ic.contains("/")) candidates.add(ic.replace("/", "-"));
                if (ic.contains("-")) candidates.add(ic.replace("-", "/"));
            }
        }

        // Try looking up certificate_no from inspection complete details tables
        String[] compTables = {
            "rail_inspection_complete_details",
            "sleeper_inspection_complete_details",
            "inspection_complete_details"
        };
        for (String t : compTables) {
            try {
                String sql = "SELECT certificate_no FROM " + t + " WHERE call_no = ? OR call_no LIKE CONCAT('%', ?, '%') ORDER BY id DESC LIMIT 1";
                List<String> certList = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("certificate_no"), callNo, callNo);
                if (certList != null && !certList.isEmpty() && certList.get(0) != null && !certList.get(0).isBlank()) {
                    String cert = certList.get(0).trim();
                    if (!candidates.contains(cert)) candidates.add(cert);
                    if (cert.contains("/")) candidates.add(cert.replace("/", "-"));
                    if (cert.contains("-")) candidates.add(cert.replace("-", "/"));
                    break;
                }
            } catch (Exception ignored) {}
        }

        String resolvedFromTables = resolveIcFromEditTables(callNo);
        if (resolvedFromTables != null && !candidates.contains(resolvedFromTables)) {
            candidates.add(resolvedFromTables);
            if (resolvedFromTables.contains("/")) candidates.add(resolvedFromTables.replace("/", "-"));
            if (resolvedFromTables.contains("-")) candidates.add(resolvedFromTables.replace("-", "/"));
        }

        for (String cand : candidates) {
            Optional<CertificateStorage> storageOpt = certificateStorageRepository.findByIcNumber(cand);
            if (storageOpt.isEmpty()) {
                storageOpt = certificateStorageRepository.findByCallNumber(cand);
            }

            if (storageOpt.isPresent()) {
                CertificateStorage storage = storageOpt.get();
                String fileName = storage.getFileName();
                if (fileName != null && !fileName.isBlank()) {
                    try {
                        byte[] data = azureBlobStorageService.downloadFile(fileName);
                        if (data != null && data.length > 0) return data;
                    } catch (Exception e) {
                        log.debug("Direct downloadFile failed for IC filename {}: {}", fileName, e.getMessage());
                    }
                    String noSpaces = fileName.replaceAll("\\s+", "");
                    if (!noSpaces.equals(fileName)) {
                        try {
                            byte[] data = azureBlobStorageService.downloadFile(noSpaces);
                            if (data != null && data.length > 0) return data;
                        } catch (Exception ignored) {}
                    }
                }
                if (storage.getBlobUrl() != null && storage.getBlobUrl().startsWith("http")) {
                    try {
                        java.net.URL url = new java.net.URI(storage.getBlobUrl()).toURL();
                        try (java.io.InputStream in = url.openStream()) {
                            byte[] data = in.readAllBytes();
                            if (data != null && data.length > 0) return data;
                        }
                    } catch (Exception ignored) {}
                }
            }

            try {
                ResponseEntity<byte[]> resp = azureBlobStorageService.openCertificate(cand);
                if (resp != null && resp.getBody() != null && resp.getBody().length > 0) {
                    return resp.getBody();
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    private String resolveIcFromEditTables(String callNo) {
        String[] tables = {
            "railpad_final_ic_edit", "sleeper_final_ic_edit",
            "final_ic_edit", "railpad_process_ic_edit", "process_ic_edit", "rm_ic_edit"
        };
        for (String table : tables) {
            try {
                String sql = "SELECT ic_number FROM " + table + " WHERE ic_number LIKE CONCAT('%', ?, '%') ORDER BY id DESC LIMIT 1";
                List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("ic_number"), callNo);
                if (list != null && !list.isEmpty() && list.get(0) != null && !list.get(0).isBlank()) {
                    return list.get(0).trim();
                }
            } catch (Exception ignored) {}
            try {
                String sql = "SELECT ic_number FROM " + table + " WHERE call_number LIKE CONCAT('%', ?, '%') ORDER BY id DESC LIMIT 1";
                List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("ic_number"), callNo);
                if (list != null && !list.isEmpty() && list.get(0) != null && !list.get(0).isBlank()) {
                    return list.get(0).trim();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private boolean isValidAzure(String connStr) {
        if (connStr == null || connStr.trim().isEmpty()) return false;
        String trimmed = connStr.trim().replace("\"", "").replace("'", "");
        return trimmed.length() >= 25 && trimmed.contains("DefaultEndpointsProtocol=");
    }

    private byte[] tryDownloadFromAzure(String connStr, String targetContainer, String blobCandidate) {
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connStr)
                    .buildClient();
            BlobContainerClient client = blobServiceClient.getBlobContainerClient(targetContainer);
            if (client != null && client.exists()) {
                BlobClient blobClient = client.getBlobClient(blobCandidate);
                if (blobClient.exists()) {
                    java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
                    blobClient.downloadStream(outputStream);
                    log.info("Downloaded correction slip PDF from Azure container '{}': {}", targetContainer, blobCandidate);
                    return outputStream.toByteArray();
                }
            }
        } catch (Exception e) {
            log.warn("Azure check for blob '{}' in container '{}' failed: {}", blobCandidate, targetContainer, e.getMessage());
        }
        return null;
    }


    @Override
    @Transactional
    public void deleteCorrectionSlip(String callNo) {
        if (callNo == null || callNo.trim().isEmpty()) {
            return;
        }
        String clean = callNo.trim();
        List<CorrectionSlipDocument> docs = getAllDocuments(clean);
        for (CorrectionSlipDocument doc : docs) {
            // Delete blob from Azure
            if (!isLocalOrInvalidAzure() && doc.getBlobFileName() != null) {
                try {
                    BlobContainerClient client = getContainerClient();
                    BlobClient blobClient = client.getBlobClient(doc.getBlobFileName());
                    if (blobClient.exists()) {
                        blobClient.delete();
                        log.info("Deleted correction slip blob '{}' from Azure container '{}'",
                                doc.getBlobFileName(), correctionSlipContainerName);
                    }
                } catch (Exception e) {
                    log.warn("Could not delete blob '{}' from Azure: {}", doc.getBlobFileName(), e.getMessage());
                }
            }
            // Delete local file if present
            if (doc.getBlobFileName() != null) {
                File localFile = new File("uploads/correction_slips", doc.getBlobFileName());
                if (localFile.exists()) {
                    localFile.delete();
                }
            }
            doc.setStatus("DELETED");
            documentRepository.save(doc);
        }
        log.info("Deleted {} correction slip document record(s) for callNo: {}", docs.size(), clean);
    }
}
