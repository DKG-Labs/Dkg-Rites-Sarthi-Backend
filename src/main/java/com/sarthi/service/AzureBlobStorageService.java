package com.sarthi.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.sarthi.entity.certificate.CertificateStorage;
import com.sarthi.repository.certificate.CertificateStorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@Slf4j
public class AzureBlobStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name:certificates}")
    private String containerName;

    private BlobContainerClient containerClient;

    @Autowired
    private CertificateStorageRepository certificateStorageRepository;

    private BlobContainerClient getContainerClient() {
        if (containerClient == null) {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            containerClient = blobServiceClient.createBlobContainerIfNotExists(containerName);
        }
        return containerClient;
    }

    /**
     * Uploads a base64 encoded PDF to Azure Blob Storage
     * 
     * @param base64Data The base64 encoded PDF content
     * @param fileName   The name of the file to store
     * @return The URL of the uploaded blob
     */
    public String uploadBase64File(String base64Data, String fileName) {
        try {
            log.info("Uploading file to Azure Blob Storage: {}", fileName);
            
            // Remove header if present (e.g., data:application/pdf;base64,)
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }
            
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
            
            BlobClient blobClient = getContainerClient().getBlobClient(fileName);
            blobClient.upload(inputStream, decodedBytes.length, true);
            
            String blobUrl = blobClient.getBlobUrl();
            log.info("File uploaded successfully. URL: {}", blobUrl);
            return blobUrl;
            
        } catch (Exception e) {
            log.error("Error uploading file to Azure: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to Azure storage", e);
        }
    }

    /**
     * Downloads a file from Azure Blob Storage as base64
     */
    public String downloadAsBase64(String fileName) {
        try {
            BlobClient blobClient = getContainerClient().getBlobClient(fileName);
            if (!blobClient.exists()) {
                throw new RuntimeException("File not found in storage: " + fileName);
            }
            
            byte[] content = blobClient.downloadContent().toBytes();
            return Base64.getEncoder().encodeToString(content);
            
        } catch (Exception e) {
            log.error("Error downloading file from Azure: {}", e.getMessage());
            throw new RuntimeException("Failed to download file from Azure", e);
        }
    }

    /**
     * Downloads a file from Azure Blob Storage as byte array
     */
    public byte[] downloadFile(String fileName) {

        try {

            BlobClient blobClient =
                    getContainerClient().getBlobClient(fileName);

            if (!blobClient.exists()) {

                throw new RuntimeException(
                        "File not found in storage: " + fileName
                );
            }

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            blobClient.download(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {

            log.error(
                    "Error downloading file from Azure: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Failed to download file from Azure",
                    e
            );
        }
    }






    public String getCertificatePath(String callNumber) {

        CertificateStorage storage =
                certificateStorageRepository
                        .findByIcNumber(callNumber)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Certificate not found"
                                )
                        );

        return "/api/certificates/view/" +
                callNumber +
                ".pdf";
    }

    public ResponseEntity<byte[]> openCertificate(
            String callNumber) {

        try {

            CertificateStorage storage =
                    certificateStorageRepository
                            .findByCallNumber(callNumber)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Certificate not found"
                                    )
                            );

            byte[] pdfBytes =
                    downloadFile(storage.getFileName());

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=" +
                                    storage.getFileName()
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);

        } catch (Exception e) {

            log.error(
                    "Failed to open certificate for {} : {}",
                    callNumber,
                    e.getMessage(),
                    e
            );

            return ResponseEntity.internalServerError()
                    .build();
        }
    }
}
