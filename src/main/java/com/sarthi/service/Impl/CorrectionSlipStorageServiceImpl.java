package com.sarthi.service.Impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.sarthi.dto.StoreCorrectionSlipRequestDTO;
import com.sarthi.dto.StoreCorrectionSlipResponseDTO;
import com.sarthi.entity.certificate.CorrectionSlipDocument;
import com.sarthi.repository.certificate.CorrectionSlipDocumentRepository;
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
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CorrectionSlipStorageServiceImpl implements CorrectionSlipStorageService {

    private final CorrectionSlipDocumentRepository documentRepository;
    private final PdfCompressionService pdfCompressionService;

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
        String stage = (request.getStage() != null ? request.getStage().trim().toUpperCase() : "PRE_SIGN");

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

        List<CorrectionSlipDocument> existingDocs = getAllDocuments(callNo);
        int nextIndex = existingDocs.size() + 1;

        String sanitizedCallNo = callNo.replaceAll("[^a-zA-Z0-9_-]", "_");
        String sanitizedFileName = String.format("Correction_Slip_%s_%d.pdf", sanitizedCallNo, nextIndex);
        String blobPath = String.format("%s/%s/%s", folderPrefix, sanitizedCallNo, sanitizedFileName);

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

        // 4. Save new CorrectionSlipDocument row in DB
        CorrectionSlipDocument doc = CorrectionSlipDocument.builder()
                .callNo(callNo)
                .status("ACTIVE")
                .icNumber(request.getIcNumber())
                .moduleType(moduleType)
                .originalFileName(sanitizedFileName)
                .blobFileName(blobPath)
                .blobUrl(blobUrl)
                .fileSizeOriginal(originalSize)
                .fileSizeCompressed(finalSize)
                .contentType("application/pdf")
                .stage(stage)
                .uploadedBy(request.getUploadedBy() != null ? request.getUploadedBy() : "Inspecting Engineer")
                .build();

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
        Optional<CorrectionSlipDocument> docOpt = documentRepository.findFirstByCallNoAndStatusOrderByUploadedAtDesc(clean, "ACTIVE");
        if (docOpt.isEmpty()) {
            docOpt = documentRepository.findFirstByCallNoAndStatusOrderByUploadedAtDesc(":" + clean, "ACTIVE");
        }
        if (docOpt.isEmpty()) {
            String alt = clean.contains(" ") ? clean.replace(" ", "-") : clean.replace("-", " ");
            docOpt = documentRepository.findFirstByCallNoAndStatusOrderByUploadedAtDesc(alt, "ACTIVE");
            if (docOpt.isEmpty()) {
                docOpt = documentRepository.findFirstByCallNoAndStatusOrderByUploadedAtDesc(":" + alt, "ACTIVE");
            }
        }
        return docOpt;
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
            variations.add(":" + clean.replace(" ", "-"));
        } else if (clean.contains("-")) {
            variations.add(clean.replace("-", " "));
            variations.add(":" + clean.replace("-", " "));
        }

        List<CorrectionSlipDocument> list = documentRepository.findByCallNoInAndStatusOrderByUploadedAtAsc(variations, "ACTIVE");
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

        Optional<CorrectionSlipDocument> optDoc = documentRepository.findByIdAndStatus(id, "ACTIVE");
        if (optDoc.isEmpty()) {
            log.warn("No correction slip document found in database for id: {}", id);
            return ResponseEntity.notFound().build();
        }

        return buildPdfResponse(optDoc.get(), inline);
    }

    private ResponseEntity<Resource> buildPdfResponse(CorrectionSlipDocument doc, boolean inline) {
        log.info("buildPdfResponse for doc id={}, callNo='{}', blobFileName='{}', blobUrl='{}', defaultContainer='{}'",
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
            log.warn("Correction slip file content could not be retrieved from local or Azure for id: {}", doc.getId());
            return ResponseEntity.notFound().build();
        }

        byte[] decompressedBytes = FileCompressionUtil.decompressIfNeeded(data);
        ByteArrayResource resource = new ByteArrayResource(decompressedBytes);

        String disposition = inline ? "inline" : "attachment";
        String filename = doc.getOriginalFileName() != null ? doc.getOriginalFileName() : ("Correction_Slip_" + doc.getCallNo() + ".pdf");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + filename + "\"")
                .body(resource);
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
