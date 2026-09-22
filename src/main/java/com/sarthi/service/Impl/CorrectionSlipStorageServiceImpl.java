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
import java.util.Base64;
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

    private BlobContainerClient getContainerClient() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        return blobServiceClient.createBlobContainerIfNotExists(correctionSlipContainerName);
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

        String fileName = request.getFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = String.format("Correction_Slip_%s.pdf", callNo.replaceAll("[^a-zA-Z0-9_-]", "_"));
        }
        String sanitizedCallNo = callNo.replaceAll("[^a-zA-Z0-9_-]", "_");
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

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

        // 4. Save/Update in DB
        CorrectionSlipDocument doc = documentRepository
                .findFirstByCallNoAndStatusOrderByUploadedAtDesc(callNo, "ACTIVE")
                .orElse(CorrectionSlipDocument.builder()
                        .callNo(callNo)
                        .status("ACTIVE")
                        .build());

        doc.setIcNumber(request.getIcNumber());
        doc.setModuleType(moduleType);
        doc.setOriginalFileName(sanitizedFileName);
        doc.setBlobFileName(blobPath);
        doc.setBlobUrl(blobUrl);
        doc.setFileSizeOriginal(originalSize);
        doc.setFileSizeCompressed(finalSize);
        doc.setContentType("application/pdf");
        doc.setStage(stage);
        doc.setUploadedBy(request.getUploadedBy() != null ? request.getUploadedBy() : "Inspecting Engineer");

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
        return documentRepository.findFirstByCallNoAndStatusOrderByUploadedAtDesc(callNo.trim(), "ACTIVE");
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

    private ResponseEntity<Resource> getPdfResource(String callNo, boolean inline) {
        if (callNo == null || callNo.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String clean = callNo.trim();
        if (clean.endsWith(".pdf")) {
            clean = clean.substring(0, clean.length() - 4);
        }

        CorrectionSlipDocument doc = documentRepository
                .findFirstByCallNoAndStatusOrderByUploadedAtDesc(clean, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("No correction slip document found for call: " + callNo));

        byte[] data = null;

        // 1. Try local file first
        if (doc.getBlobFileName() != null) {
            File localFile = new File("uploads/correction_slips", doc.getBlobFileName());
            if (localFile.exists()) {
                try {
                    data = Files.readAllBytes(localFile.toPath());
                } catch (IOException e) {
                    log.error("Could not read local correction slip file: {}", e.getMessage());
                }
            }
        }

        // 2. Try Azure Blob Storage
        if (data == null && !isLocalOrInvalidAzure() && doc.getBlobFileName() != null) {
            try {
                BlobContainerClient containerClient = getContainerClient();
                BlobClient blobClient = containerClient.getBlobClient(doc.getBlobFileName());
                if (blobClient.exists()) {
                    data = blobClient.downloadContent().toBytes();
                }
            } catch (Exception e) {
                log.warn("Could not download correction slip from Azure container '{}': {}",
                        correctionSlipContainerName, e.getMessage());
            }
        }

        if (data == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] decompressedBytes = FileCompressionUtil.decompressIfNeeded(data);
        ByteArrayResource resource = new ByteArrayResource(decompressedBytes);

        String disposition = inline ? "inline" : "attachment";
        String filename = doc.getOriginalFileName() != null ? doc.getOriginalFileName() : ("Correction_Slip_" + clean + ".pdf");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + filename + "\"")
                .body(resource);
    }
}
