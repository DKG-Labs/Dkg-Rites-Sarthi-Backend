package com.sarthi.service.Impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.sarthi.entity.certificate.IcAnnexureDocument;
import com.sarthi.repository.certificate.IcAnnexureDocumentRepository;
import com.sarthi.service.IcAnnexureService;
import com.sarthi.service.PdfCompressionService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class IcAnnexureServiceImpl implements IcAnnexureService {

    private final IcAnnexureDocumentRepository annexureRepository;
    private final PdfCompressionService pdfCompressionService;

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.annexures-container-name:sarthi-uploads}")
    private String annexuresContainerName;

    private static final long MAX_SIZE_ERC_BYTES = 20 * 1024 * 1024L;      // 20 MB
    private static final long MAX_SIZE_RAILPAD_BYTES = 20 * 1024 * 1024L;  // 20 MB
    private static final long MAX_SIZE_SLEEPER_BYTES = 50 * 1024 * 1024L;  // 50 MB

    private volatile BlobContainerClient containerClient;

    private BlobContainerClient getContainerClient() {
        if (containerClient == null) {
            synchronized (this) {
                if (containerClient == null) {
                    BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                            .connectionString(connectionString)
                            .buildClient();
                    BlobContainerClient client = blobServiceClient.getBlobContainerClient(annexuresContainerName);
                    try {
                        client.createIfNotExists();
                    } catch (Exception e) {
                        log.debug("Container already exists or verified: {}", e.getMessage());
                    }
                    containerClient = client;
                }
            }
        }
        return containerClient;
    }


    @Override
    @Transactional
    public IcAnnexureDocument uploadAnnexure(
            MultipartFile file,
            String callNo,
            String icNumber,
            String moduleType,
            String uploadedBy
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (callNo == null || callNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Call Number is required");
        }

        String normalizedModule = (moduleType != null ? moduleType.trim().toUpperCase() : "SLEEPER");
        String folderPrefix = com.sarthi.util.BlobFolderResolver.resolveFolder(callNo, normalizedModule);

        long originalSize = file.getSize();

        // Check module-specific size limits
        long maxSizeAllowed = MAX_SIZE_SLEEPER_BYTES;
        if ("ERC".equals(normalizedModule)) {
            maxSizeAllowed = MAX_SIZE_ERC_BYTES;
        } else if ("RAILPAD".equals(normalizedModule)) {
            maxSizeAllowed = MAX_SIZE_RAILPAD_BYTES;
        }

        if (originalSize > maxSizeAllowed) {
            long maxMb = maxSizeAllowed / (1024 * 1024);
            throw new IllegalArgumentException(String.format(
                    "File size exceeds maximum allowed limit for %s (%d MB)", normalizedModule, maxMb));
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "annexure.pdf";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/pdf";

        // Strictly allow only PDF format
        if (!originalFilename.toLowerCase().endsWith(".pdf") && !contentType.equalsIgnoreCase("application/pdf")) {
            throw new IllegalArgumentException("Only PDF documents (.pdf) are allowed for annexures and docs upload.");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content", e);
        }

        // Fast PDF optimization
        byte[] finalBytes = pdfCompressionService.compressPdf(fileBytes);
        if (finalBytes == null || finalBytes.length == 0) {
            finalBytes = fileBytes;
        }

        long compressedSize = finalBytes.length;
        String sanitizedCallNo = callNo.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
        String sanitizedFileName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String blobPath = String.format("%s/%s/%d_%s", folderPrefix, sanitizedCallNo, System.currentTimeMillis(), sanitizedFileName);

        String blobUrl;
        try {
            BlobContainerClient client = getContainerClient();
            BlobClient blobClient = client.getBlobClient(blobPath);
            blobClient.upload(new ByteArrayInputStream(finalBytes), finalBytes.length, true);
            blobUrl = blobClient.getBlobUrl();
            log.info("Uploaded annexure to Azure Blob: {}", blobUrl);
        } catch (Exception e) {
            log.warn("Azure upload failed, saving to local storage: {}", e.getMessage());
            blobUrl = saveToLocal(blobPath, finalBytes);
        }

        IcAnnexureDocument doc = IcAnnexureDocument.builder()
                .callNo(callNo.trim())
                .icNumber(icNumber != null && !icNumber.trim().isEmpty() ? icNumber.trim() : null)
                .moduleType(normalizedModule)
                .originalFileName(originalFilename)
                .blobFileName(blobPath)
                .blobUrl(blobUrl)
                .fileSizeOriginal(originalSize)
                .fileSizeCompressed(compressedSize)
                .contentType(contentType)
                .uploadedBy(uploadedBy != null ? uploadedBy : "Inspecting Engineer")
                .status("ACTIVE")
                .build();

        return annexureRepository.save(doc);
    }

    private String saveToLocal(String relativePath, byte[] data) {
        try {
            File targetFile = new File("uploads/annexures", relativePath);
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(data);
            }
            return "/uploads/annexures/" + relativePath.replace('\\', '/');
        } catch (IOException e) {
            log.error("Error saving local annexure: {}", e.getMessage(), e);
            return "local://" + relativePath;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<IcAnnexureDocument> getAnnexuresByCall(String callNo, String moduleType) {
        if (callNo == null || callNo.trim().isEmpty()) {
            return List.of();
        }
        String cleanCall = callNo.trim();
        List<IcAnnexureDocument> docs = List.of();
        if (moduleType != null && !moduleType.trim().isEmpty()) {
            docs = annexureRepository.findByCallNoAndModuleTypeAndStatusOrderByUploadedAtDesc(
                    cleanCall, moduleType.trim().toUpperCase(), "ACTIVE");
        }
        if (docs.isEmpty()) {
            docs = annexureRepository.findByCallNoAndStatusOrderByUploadedAtDesc(cleanCall, "ACTIVE");
        }
        if (docs.isEmpty()) {
            // Try hyphen to slash and slash to hyphen variations
            String altCall = cleanCall.contains("-") ? cleanCall.replace("-", "/") : cleanCall.replace("/", "-");
            docs = annexureRepository.findByCallNoAndStatusOrderByUploadedAtDesc(altCall, "ACTIVE");
        }
        if (docs.isEmpty()) {
            // Also try matching against icNumber in case callNo was stored under icNumber
            docs = annexureRepository.findByIcNumberAndStatusOrderByUploadedAtDesc(cleanCall, "ACTIVE");
        }
        return docs;
    }

    @Override
    @Transactional
    public void deleteAnnexure(Long id, String requestedBy) {
        IcAnnexureDocument doc = annexureRepository.findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("Annexure document not found with ID: " + id));

        doc.setStatus("DELETED");
        annexureRepository.save(doc);

        // Optionally delete blob if in Azure
        if (doc.getBlobFileName() != null) {
            try {
                BlobContainerClient containerClient = getContainerClient();
                BlobClient blobClient = containerClient.getBlobClient(doc.getBlobFileName());
                if (blobClient.exists()) {
                    blobClient.delete();
                }
            } catch (Exception e) {
                log.warn("Could not delete blob {}: {}", doc.getBlobFileName(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadAnnexure(Long id) {
        IcAnnexureDocument doc = annexureRepository.findByIdAndStatus(id, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("Annexure document not found with ID: " + id));

        byte[] data = null;

        // 1. Try local storage first for instant retrieval
        if (doc.getBlobFileName() != null) {
            File localFile = new File("uploads/annexures", doc.getBlobFileName());
            if (localFile.exists()) {
                try {
                    data = Files.readAllBytes(localFile.toPath());
                } catch (IOException e) {
                    log.error("Could not read local file: {}", e.getMessage());
                }
            }
        }

        // 2. Fetch from Azure Blob Storage (same standard way as IC download in AzureBlobStorageService)
        if (data == null && doc.getBlobFileName() != null) {
            try {
                BlobContainerClient client = getContainerClient();
                BlobClient blobClient = client.getBlobClient(doc.getBlobFileName());
                if (blobClient.exists()) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    blobClient.download(outputStream);
                    data = outputStream.toByteArray();
                }
            } catch (Exception e) {
                log.warn("Could not download annexure from Azure: {}", e.getMessage());
            }
        }

        if (data == null) {
            throw new IllegalArgumentException("Annexure file content is not available locally or in cloud storage for ID: " + id);
        }

        // Cache downloaded bytes locally so subsequent requests on local are instant
        if (doc.getBlobFileName() != null) {
            try {
                File localFile = new File("uploads/annexures", doc.getBlobFileName());
                if (!localFile.exists()) {
                    File parent = localFile.getParentFile();
                    if (parent != null && !parent.exists()) parent.mkdirs();
                    Files.write(localFile.toPath(), data);
                }
            } catch (Exception ex) {
                log.debug("Could not cache file locally: {}", ex.getMessage());
            }
        }

        // Decompress on-the-fly for view/download if compressed
        byte[] decompressedBytes = FileCompressionUtil.decompressIfNeeded(data);

        ByteArrayResource resource = new ByteArrayResource(decompressedBytes);
        String contentType = doc.getContentType() != null ? doc.getContentType() : "application/pdf";
        String filename = doc.getOriginalFileName() != null ? doc.getOriginalFileName() : "annexure.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
