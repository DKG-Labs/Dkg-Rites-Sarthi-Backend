package com.sarthi.udmcomplaint.service;

import com.sarthi.constant.AppConstant;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.udmcomplaint.UdmComplaintConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Stores UDM complaint PDFs in their own private Azure container.
 * Rules: PDF only (extension, content type and %PDF header), max 5 MB.
 * If the surrounding DB transaction rolls back, the uploaded blob is deleted again.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UdmComplaintDocumentStorage {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final AzureBlobStorageService blobStorage;

    @Value("${azure.storage.udm-complaint-docs-container-name:sarthi-udm-complaint-docs}")
    private String containerName;

    /** Validated PDF ready to upload. */
    public record ValidPdf(String originalName, byte[] bytes) { }

    /** Validates a PDF upload; returns null when the file is optional and missing. */
    public ValidPdf validate(MultipartFile file, boolean required, String label) {
        if (file == null || file.isEmpty()) {
            if (required) throw validation(label + " (PDF) is required.");
            return null;
        }
        if (file.getSize() > UdmComplaintConstants.DOC_MAX_BYTES) {
            throw validation(label + " must be 5 MB or smaller.");
        }
        String name = file.getOriginalFilename() == null ? "document.pdf" : file.getOriginalFilename().trim();
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf") || !(type.isEmpty() || type.contains("pdf"))) {
            throw validation(label + " must be a PDF file.");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw validation("Could not read " + label + ". Please try again.");
        }
        // Real PDF check: file must start with %PDF
        if (bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D' || bytes[3] != 'F') {
            throw validation(label + " is not a valid PDF file.");
        }
        return new ValidPdf(name, bytes);
    }

    /**
     * Uploads to udm-complaints/{rejectionId}/{stage}/{timestamp}_{name}.pdf and returns the blob name.
     * Registers a rollback hook so a failed DB save does not leave an orphan file.
     */
    public String upload(ValidPdf pdf, Long udmRejectionId, String stage) {
        String safe = pdf.originalName().replaceAll("[^a-zA-Z0-9._\\-]", "_");
        if (safe.length() > 120) safe = safe.substring(safe.length() - 120);
        String blobName = "udm-complaints/" + udmRejectionId + "/" + stage.toLowerCase(Locale.ROOT) + "/"
                + LocalDateTime.now().format(TS) + "_" + safe;
        try {
            blobStorage.uploadFileBytes(pdf.bytes(), blobName, containerName);
        } catch (RuntimeException e) {
            log.error("[UDM-COMPLAINT] upload failed for {} {}: {}", udmRejectionId, stage, e.getMessage());
            throw validation("Document upload failed. Please try again.");
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        try {
                            blobStorage.deleteFile(blobName, containerName);
                        } catch (RuntimeException ex) {
                            log.warn("[UDM-COMPLAINT] could not remove orphan blob {}: {}", blobName, ex.getMessage());
                        }
                    }
                }
            });
        }
        return blobName;
    }

    public byte[] download(String blobName) {
        byte[] data = blobStorage.downloadFileFromContainer(blobName, containerName);
        if (data == null || data.length == 0) throw validation("Document not found in storage.");
        return data;
    }

    private static BusinessException validation(String message) {
        return new BusinessException(new ErrorDetails(
                AppConstant.ERROR_CODE_RESOURCE, AppConstant.ERROR_TYPE_CODE_RESOURCE,
                AppConstant.ERROR_TYPE_VALIDATION, message));
    }
}
