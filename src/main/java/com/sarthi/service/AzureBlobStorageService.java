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
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.util.Iterator;

@Service
@Slf4j
public class AzureBlobStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
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

    private BlobContainerClient getContainerClient(String targetContainerName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        return blobServiceClient.createBlobContainerIfNotExists(targetContainerName);
    }

    /**
     * Uploads a base64 encoded PDF to Azure Blob Storage
     * 
     * @param base64Data The base64 encoded PDF content
     * @param fileName   The name of the file to store
     * @return The URL of the uploaded blob
     */
    public String uploadBase64File(String base64Data, String fileName) {
        return uploadBase64File(base64Data, fileName, this.containerName);
    }

    /**
     * Uploads a base64 encoded file to a specific Azure Blob Storage container
     * 
     * @param base64Data          The base64 encoded file content
     * @param fileName            The name of the file to store
     * @param targetContainerName The specific container to upload to
     * @return The URL of the uploaded blob
     */
    public String uploadBase64File(String base64Data, String fileName, String targetContainerName) {
        try {
            log.info("Uploading file to Azure Blob Storage container '{}': {}", targetContainerName, fileName);
            
            // Remove header if present (e.g., data:image/png;base64,)
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }
            
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            
            boolean isImage = fileName.toLowerCase().endsWith(".jpg") || 
                              fileName.toLowerCase().endsWith(".jpeg") || 
                              fileName.toLowerCase().endsWith(".png");
            
            if (isImage) {
                decodedBytes = compressImage(decodedBytes);
            }
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes);
            
            BlobClient blobClient = getContainerClient(targetContainerName).getBlobClient(fileName);
            blobClient.upload(inputStream, decodedBytes.length, true);
            
            String blobUrl = blobClient.getBlobUrl();
            log.info("File uploaded successfully. URL: {}", blobUrl);
            return blobUrl;
            
        } catch (Exception e) {
            log.error("Error uploading file to Azure: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to Azure storage", e);
        }
    }

    private byte[] compressImage(byte[] imageBytes) {
        try {
            // Read image from byte array
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                log.warn("Could not read image for compression. Uploading raw bytes.");
                return imageBytes;
            }

            // Paint transparent background to white if image has transparency channel (for JPEG compatibility)
            if (image.getType() == BufferedImage.TYPE_INT_ARGB || image.getColorModel().hasAlpha()) {
                BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(java.awt.Color.WHITE);
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
                image = rgbImage;
            }

            // Target size in bytes: 50 KB
            int targetSize = 50 * 1024;
            
            // If the original image is already smaller than target, return as-is
            if (imageBytes.length <= targetSize) {
                log.info("Image size ({} KB) is below target. Skipping compression.", imageBytes.length / 1024);
                return imageBytes;
            }

            byte[] compressedBytes = imageBytes;
            float quality = 0.8f;
            
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            if (!writers.hasNext()) {
                log.warn("No JPEG writer found. Uploading raw bytes.");
                return imageBytes;
            }
            ImageWriter writer = writers.next();

            while (quality >= 0.1f) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageOutputStream ios = ImageIO.createImageOutputStream(bos);
                writer.setOutput(ios);

                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionType(param.getCompressionTypes()[0]);
                    param.setCompressionQuality(quality);
                }

                writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
                ios.flush();
                ios.close();
                
                byte[] tempBytes = bos.toByteArray();
                
                // If it meets the target size, or we are at minimum quality, we use it
                if (tempBytes.length <= targetSize || quality <= 0.15f) {
                    compressedBytes = tempBytes;
                    break;
                }
                
                // Otherwise reduce quality
                quality -= 0.15f;
            }
            
            writer.dispose();
            log.info("Compressed image from {} KB to {} KB", imageBytes.length / 1024, compressedBytes.length / 1024);
            return compressedBytes;

        } catch (Exception e) {
            log.error("Failed to compress image: {}", e.getMessage(), e);
            return imageBytes;
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
