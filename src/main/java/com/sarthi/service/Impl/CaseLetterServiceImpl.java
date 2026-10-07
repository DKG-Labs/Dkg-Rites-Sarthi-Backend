package com.sarthi.service.Impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.sarthi.entity.certificate.CaseLetterDocument;
import com.sarthi.repository.certificate.CaseLetterDocumentRepository;
import com.sarthi.repository.certificate.CertificateStorageRepository;
import com.sarthi.service.CaseLetterService;
import com.sarthi.service.PdfCompressionService;
import com.sarthi.service.VendorInspectionCallService;
import com.sarthi.util.BlobFolderResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Slf4j
public class CaseLetterServiceImpl implements CaseLetterService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.case-letter-container-name:sarthi-case-letter}")
    private String caseLetterContainerName;

    private BlobContainerClient containerClient;

    @Autowired
    private CaseLetterDocumentRepository caseLetterDocumentRepository;

    @Autowired
    private CertificateStorageRepository certificateStorageRepository;

    @Autowired
    private VendorInspectionCallService vendorInspectionCallService;

    @Autowired
    private PdfCompressionService pdfCompressionService;

    private synchronized BlobContainerClient getContainerClient() {
        if (containerClient == null) {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            String effectiveContainer = (caseLetterContainerName != null && !caseLetterContainerName.isBlank())
                    ? caseLetterContainerName.trim()
                    : "sarthi-case-letter";
            try {
                containerClient = blobServiceClient.getBlobContainerClient(effectiveContainer);
                if (!containerClient.exists()) {
                    containerClient = blobServiceClient.createBlobContainer(effectiveContainer);
                }
            } catch (Exception e) {
                log.warn("Could not check/create container {}, will try on demand: {}", effectiveContainer, e.getMessage());
                containerClient = blobServiceClient.getBlobContainerClient(effectiveContainer);
            }
        }
        return containerClient;
    }

    @Override
    public Map<String, Object> getCallDocumentsMetadata(String callNo, String moduleType) {
        Map<String, Object> result = new HashMap<>();
        if (callNo == null || callNo.trim().isEmpty()) {
            return result;
        }

        String cleanCallNo = callNo.trim();
        result.put("callNo", cleanCallNo);
        result.put("moduleType", moduleType);

        // 1. Check if Case Letter was already saved
        Optional<CaseLetterDocument> savedDocOpt = findActiveCaseLetter(cleanCallNo);
        if (savedDocOpt.isPresent()) {
            CaseLetterDocument doc = savedDocOpt.get();
            Map<String, Object> savedMeta = new HashMap<>();
            savedMeta.put("id", doc.getId());
            savedMeta.put("originalFileName", doc.getOriginalFileName());
            savedMeta.put("blobFileName", doc.getBlobFileName());
            savedMeta.put("blobUrl", doc.getBlobUrl());
            savedMeta.put("fileSizeCompressed", doc.getFileSizeCompressed());
            savedMeta.put("fileSizeOriginal", doc.getFileSizeOriginal());
            savedMeta.put("uploadedAt", doc.getUploadedAt());
            savedMeta.put("uploadedBy", doc.getUploadedBy());
            savedMeta.put("stage", doc.getStage());
            result.put("existingCaseLetter", savedMeta);
        } else {
            result.put("existingCaseLetter", null);
        }

        // 2. Check IC availability
        boolean icAvailable = certificateStorageRepository.findByIcNumber(cleanCallNo).isPresent()
                || certificateStorageRepository.findByCallNumber(cleanCallNo).isPresent();
        result.put("isIcAvailable", icAvailable);

        // 3. Check TC availability for ER calls
        boolean tcAvailable = false;
        if (cleanCallNo.startsWith("ER-") || cleanCallNo.startsWith("ER_") || cleanCallNo.contains("ER")) {
            try {
                byte[] tcBytes = vendorInspectionCallService.getTcDocsByCallNo(cleanCallNo);
                tcAvailable = (tcBytes != null && tcBytes.length > 0);
            } catch (Exception ignored) {
                tcAvailable = false;
            }
        }
        result.put("isTcAvailable", tcAvailable);

        return result;
    }

    @Override
    public byte[] mergePdfs(List<byte[]> pdfByteList) {
        if (pdfByteList == null || pdfByteList.isEmpty()) {
            throw new IllegalArgumentException("No PDF files provided to merge");
        }

        try (PDDocument mergedDoc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDFMergerUtility merger = new PDFMergerUtility();
            int validDocCount = 0;

            for (byte[] bytes : pdfByteList) {
                if (bytes != null && bytes.length > 0) {
                    try (PDDocument sourceDoc = Loader.loadPDF(new RandomAccessReadBuffer(bytes))) {
                        merger.appendDocument(mergedDoc, sourceDoc);
                        validDocCount++;
                    } catch (Exception docEx) {
                        log.warn("Error appending document with PDFMergerUtility, attempting fallback page copy: {}", docEx.getMessage());
                        try (PDDocument sourceDoc = Loader.loadPDF(new RandomAccessReadBuffer(bytes))) {
                            for (PDPage page : sourceDoc.getPages()) {
                                mergedDoc.addPage(page);
                            }
                            validDocCount++;
                        } catch (Exception fallbackEx) {
                            log.error("Failed to append PDF bytes: {}", fallbackEx.getMessage());
                        }
                    }
                }
            }

            if (validDocCount == 0) {
                throw new IllegalArgumentException("All provided PDF files were empty or invalid");
            }

            mergedDoc.save(out, CompressParameters.DEFAULT_COMPRESSION);
            log.info("Merged {} PDF documents successfully. Output size: {} bytes", validDocCount, out.size());
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error merging PDFs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to merge PDF documents: " + e.getMessage(), e);
        }
    }

    private static final long MAX_INDIVIDUAL_FILE_SIZE = 3L * 1024 * 1024; // 3 MB

    @Override
    public byte[] mergeUploadedFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files uploaded for merging");
        }

        List<byte[]> byteList = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
                String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

                // 1. Strictly enforce PDF format
                if (!filename.endsWith(".pdf") && !contentType.equals("application/pdf")) {
                    throw new IllegalArgumentException("Only PDF format (.pdf) is allowed. Invalid file: " + file.getOriginalFilename());
                }

                // 2. Strictly enforce 3 MB file size limit
                if (file.getSize() > MAX_INDIVIDUAL_FILE_SIZE) {
                    throw new IllegalArgumentException(String.format("File '%s' exceeds the maximum allowed upload limit of 3 MB (Size: %.2f MB).",
                            file.getOriginalFilename(), file.getSize() / (1024.0 * 1024.0)));
                }

                try {
                    byteList.add(file.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read file: " + file.getOriginalFilename(), e);
                }
            }
        }

        return mergePdfs(byteList);
    }

    @Override
    @Transactional
    public CaseLetterDocument saveCaseLetter(
            List<MultipartFile> files,
            String callNo,
            String icNumber,
            String moduleType,
            String stage,
            String uploadedBy
    ) {
        if (callNo == null || callNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Call Number is required");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one document file is required to generate Case Letter");
        }

        String cleanCallNo = callNo.trim();
        String normalizedModule = (moduleType != null ? moduleType.trim().toUpperCase() : "ERC");
        String effectiveStage = resolveStage(cleanCallNo, normalizedModule, stage);

        // 1. Calculate total uncompressed size of all input files
        long originalInputSize = 0L;
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                originalInputSize += f.getSize();
            }
        }

        // 2. Merge all input PDFs
        byte[] mergedBytes = mergeUploadedFiles(files);

        // 3. Compress the merged PDF (downsampling embedded images and optimizing streams)
        byte[] compressedBytes = pdfCompressionService.compressPdf(mergedBytes);
        if (compressedBytes == null || compressedBytes.length == 0) {
            compressedBytes = mergedBytes;
        }
        long compressedSize = compressedBytes.length;
        long originalSize = originalInputSize > 0 ? originalInputSize : mergedBytes.length;

        // 3. Resolve folder structure: ERC -> erc/ER, erc/EP, erc/EF; Sleeper -> sleeper/SF; Railpad -> railpad/RPP, railpad/RPF
        String folderPrefix = resolveCaseLetterFolder(cleanCallNo, normalizedModule, effectiveStage);
        String sanitizedCallNo = cleanCallNo.replaceAll("[^a-zA-Z0-9_-]", "_");
        String fileName = String.format("Case_Letter_%s_%d.pdf", sanitizedCallNo, System.currentTimeMillis());
        String blobPath = String.format("%s/%s/%s", folderPrefix, sanitizedCallNo, fileName);

        // 4. Upload to Azure Blob Storage container (sarthi-case-letter)
        String blobUrl;
        try {
            BlobContainerClient client = getContainerClient();
            BlobClient blobClient = client.getBlobClient(blobPath);
            blobClient.upload(new ByteArrayInputStream(compressedBytes), compressedBytes.length, true);
            blobUrl = blobClient.getBlobUrl();
            log.info("Successfully uploaded Case Letter to Azure Blob: {}", blobUrl);
        } catch (Exception e) {
            log.warn("Azure upload failed, saving to local storage: {}", e.getMessage());
            blobUrl = saveToLocal(blobPath, compressedBytes);
        }

        // 5. Deactivate previous active Case Letter for this call (if any)
        List<CaseLetterDocument> existing = caseLetterDocumentRepository.findByCallNoAndStatus(cleanCallNo, "ACTIVE");
        if (existing != null && !existing.isEmpty()) {
            for (CaseLetterDocument oldDoc : existing) {
                oldDoc.setStatus("SUPERSEDED");
            }
            caseLetterDocumentRepository.saveAll(existing);
        }

        // 6. Save CaseLetterDocument record
        CaseLetterDocument doc = CaseLetterDocument.builder()
                .callNo(cleanCallNo)
                .icNumber(icNumber != null && !icNumber.trim().isEmpty() ? icNumber.trim() : null)
                .moduleType(normalizedModule)
                .stage(effectiveStage)
                .originalFileName(fileName)
                .blobFileName(blobPath)
                .blobUrl(blobUrl)
                .fileSizeOriginal(originalSize)
                .fileSizeCompressed(compressedSize)
                .contentType("application/pdf")
                .mergedDocCount(files.size())
                .uploadedBy(uploadedBy != null && !uploadedBy.isBlank() ? uploadedBy.trim() : "Inspecting Engineer")
                .status("ACTIVE")
                .build();

        return caseLetterDocumentRepository.save(doc);
    }

    private Optional<CaseLetterDocument> findActiveCaseLetter(String callNo) {
        if (callNo == null || callNo.isBlank()) return Optional.empty();
        String clean = callNo.trim();
        Optional<CaseLetterDocument> doc = caseLetterDocumentRepository.findFirstByCallNoAndStatusOrderByUploadedAtDesc(clean, "ACTIVE");
        if (doc.isPresent()) return doc;

        String alt1 = clean.replace("-", "/");
        doc = caseLetterDocumentRepository.findFirstByCallNoAndStatusOrderByUploadedAtDesc(alt1, "ACTIVE");
        if (doc.isPresent()) return doc;

        String alt2 = clean.replace("/", "-");
        doc = caseLetterDocumentRepository.findFirstByCallNoAndStatusOrderByUploadedAtDesc(alt2, "ACTIVE");
        if (doc.isPresent()) return doc;

        return Optional.empty();
    }

    @Override
    public CaseLetterDocument getLatestCaseLetter(String callNo) {
        if (callNo == null || callNo.trim().isEmpty()) {
            return null;
        }
        return findActiveCaseLetter(callNo).orElse(null);
    }

    @Override
    public byte[] getCaseLetterPdf(Long id) {
        CaseLetterDocument doc = caseLetterDocumentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Case Letter not found with ID: " + id));
        return downloadBlobBytes(doc.getBlobFileName());
    }

    @Override
    public byte[] getLatestCaseLetterPdf(String callNo) {
        CaseLetterDocument doc = getLatestCaseLetter(callNo);
        if (doc == null) {
            throw new NoSuchElementException("No Case Letter found for call: " + callNo);
        }
        return downloadBlobBytes(doc.getBlobFileName());
    }

    private byte[] downloadBlobBytes(String blobPath) {
        if (blobPath == null || blobPath.isBlank()) {
            throw new IllegalArgumentException("Blob path is required");
        }

        // 1. Try Azure Blob Storage
        try {
            BlobContainerClient client = getContainerClient();
            BlobClient blobClient = client.getBlobClient(blobPath);
            if (blobClient.exists()) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                blobClient.downloadStream(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            log.warn("Error downloading from Azure blob: {}", e.getMessage());
        }

        // 2. Fallback to Local Storage
        try {
            String sanitizedPath = blobPath.replaceAll("\\\\", "/");
            Path localFilePath = Paths.get(System.getProperty("user.dir"), "uploads", "case-letters", sanitizedPath);
            if (Files.exists(localFilePath)) {
                return Files.readAllBytes(localFilePath);
            }
        } catch (Exception e) {
            log.warn("Error reading local case letter fallback: {}", e.getMessage());
        }

        throw new RuntimeException("Case Letter file could not be retrieved: " + blobPath);
    }

    private String resolveStage(String callNo, String moduleType, String stageParam) {
        if (stageParam != null && !stageParam.isBlank()) {
            return stageParam.trim().toUpperCase();
        }
        String c = callNo.toUpperCase();
        if (c.startsWith("ER-") || c.startsWith("ER_") || c.contains("/ER-") || c.contains("_ER-")) return "ER";
        if (c.startsWith("EP-") || c.startsWith("EP_") || c.contains("/EP-") || c.contains("_EP-")) return "EP";
        if (c.startsWith("EF-") || c.startsWith("EF_") || c.contains("/EF-") || c.contains("_EF-")) return "EF";
        if (c.startsWith("SF") || c.startsWith("SL") || moduleType.contains("SLEEPER")) return "SF";
        if (c.startsWith("RPP")) return "RPP";
        if (c.startsWith("RPF")) return "RPF";
        return "EF";
    }

    private String resolveCaseLetterFolder(String callNo, String moduleType, String stage) {
        String m = (moduleType != null ? moduleType.trim().toUpperCase() : "");
        String s = (stage != null ? stage.trim().toUpperCase() : "");
        String c = (callNo != null ? callNo.trim().toUpperCase() : "");

        if (c.startsWith("SF") || s.equals("SF") || m.contains("SLEEPER")) {
            return "sleeper/SF";
        }
        if (c.startsWith("RPP") || s.equals("RPP")) {
            return "railpad/RPP";
        }
        if (c.startsWith("RPF") || s.equals("RPF") || m.contains("RAILPAD")) {
            return "railpad/RPF";
        }
        if (c.startsWith("ER-") || c.startsWith("ER_") || s.equals("ER")) {
            return "erc/ER";
        }
        if (c.startsWith("EP-") || c.startsWith("EP_") || s.equals("EP")) {
            return "erc/EP";
        }
        return "erc/EF";
    }

    @Override
    @Transactional
    public boolean deleteCaseLetter(String callNo, String requestedBy) {
        if (callNo == null || callNo.isBlank()) return false;
        String clean = callNo.trim();
        List<CaseLetterDocument> activeDocs = caseLetterDocumentRepository.findByCallNoAndStatus(clean, "ACTIVE");
        if (activeDocs == null || activeDocs.isEmpty()) {
            String alt1 = clean.replace("-", "/");
            activeDocs = caseLetterDocumentRepository.findByCallNoAndStatus(alt1, "ACTIVE");
        }
        if (activeDocs == null || activeDocs.isEmpty()) {
            String alt2 = clean.replace("/", "-");
            activeDocs = caseLetterDocumentRepository.findByCallNoAndStatus(alt2, "ACTIVE");
        }

        if (activeDocs == null || activeDocs.isEmpty()) {
            return false;
        }

        for (CaseLetterDocument doc : activeDocs) {
            doc.setStatus("DELETED");
            if (doc.getBlobFileName() != null) {
                try {
                    BlobContainerClient client = getContainerClient();
                    BlobClient blobClient = client.getBlobClient(doc.getBlobFileName());
                    if (blobClient.exists()) {
                        blobClient.delete();
                    }
                } catch (Exception e) {
                    log.warn("Could not delete blob {}: {}", doc.getBlobFileName(), e.getMessage());
                }
            }
        }
        caseLetterDocumentRepository.saveAll(activeDocs);
        log.info("Deleted Case Letter for callNo {} requested by {}", callNo, requestedBy);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteCaseLetterById(Long id, String requestedBy) {
        if (id == null) return false;
        Optional<CaseLetterDocument> opt = caseLetterDocumentRepository.findById(id);
        if (opt.isEmpty()) return false;

        CaseLetterDocument doc = opt.get();
        doc.setStatus("DELETED");
        if (doc.getBlobFileName() != null) {
            try {
                BlobContainerClient client = getContainerClient();
                BlobClient blobClient = client.getBlobClient(doc.getBlobFileName());
                if (blobClient.exists()) {
                    blobClient.delete();
                }
            } catch (Exception e) {
                log.warn("Could not delete blob {}: {}", doc.getBlobFileName(), e.getMessage());
            }
        }
        caseLetterDocumentRepository.save(doc);
        log.info("Deleted Case Letter ID {} requested by {}", id, requestedBy);
        return true;
    }

    private String saveToLocal(String blobPath, byte[] data) {
        try {
            String sanitizedPath = blobPath.replaceAll("\\\\", "/");
            Path targetPath = Paths.get(System.getProperty("user.dir"), "uploads", "case-letters", sanitizedPath);
            File parentDir = targetPath.getParent().toFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
                fos.write(data);
            }
            return targetPath.toUri().toString();
        } catch (Exception e) {
            log.error("Failed to save case letter locally: {}", e.getMessage(), e);
            return "";
        }
    }
}
