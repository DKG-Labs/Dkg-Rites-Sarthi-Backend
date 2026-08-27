package com.sarthi.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.sarthi.entity.certificate.CertificateStorage;
import com.sarthi.repository.certificate.CertificateStorageRepository;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.awt.image.BufferedImage;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;


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



    public byte[] decodeBase64(String base64) {

        if(base64.contains(",")){
            base64 = base64.split(",")[1];
        }

        return java.util.Base64.getDecoder().decode(base64);

    }

    private BufferedImage resizeImage(BufferedImage image)
            throws IOException {

        if(image.getWidth() <= 1024){
            return image;
        }

        double ratio =
                (double) image.getHeight() / image.getWidth();

        int newHeight =
                (int)(1024 * ratio);

        ByteArrayOutputStream bos =
                new ByteArrayOutputStream();

        Thumbnails.of(image)
                .size(1024,newHeight)
                .outputFormat("jpg")
                .toOutputStream(bos);

        return ImageIO.read(
                new ByteArrayInputStream(
                        bos.toByteArray()));
    }
    public String uploadBase64File(String base64Data, String fileName, String targetContainerName) {
        try {
            log.info("Uploading file to Azure Blob Storage container '{}': {}", targetContainerName, fileName);
            
            // Remove header if present (e.g., data:image/png;base64,)
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }
            
           // byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            
            boolean isImage = fileName.toLowerCase().endsWith(".jpg") || 
                              fileName.toLowerCase().endsWith(".jpeg") || 
                              fileName.toLowerCase().endsWith(".png");
            
//            if (isImage) {
//                decodedBytes = compressImage(decodedBytes);
//            }

            byte[] decodedBytes = decodeBase64(base64Data);

            if (isImage) {
                decodedBytes = compressToTargetSize(decodedBytes, 20); // target 20 KB
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

    /**
     * Uploads raw bytes to Azure Blob Storage
     *
     * @param fileBytes The byte array to upload
     * @param fileName The name of the file to store
     * @param targetContainerName The container name
     * @return The URL of the uploaded blob
     */
    public String uploadFileBytes(byte[] fileBytes, String fileName, String targetContainerName) {
        try {
            log.info("Uploading file bytes to Azure Blob Storage container '{}': {}", targetContainerName, fileName);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
            BlobClient blobClient = getContainerClient(targetContainerName).getBlobClient(fileName);
            blobClient.upload(inputStream, fileBytes.length, true);
            String blobUrl = blobClient.getBlobUrl();
            log.info("File uploaded successfully. URL: {}", blobUrl);
            return blobUrl;
        } catch (Exception e) {
            log.error("Error uploading file bytes to Azure: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file bytes to Azure storage", e);
        }
    }
    
    public String uploadFileBytes(byte[] fileBytes, String fileName) {
        return uploadFileBytes(fileBytes, fileName, this.containerName);
    }

    /**
     * Deletes a file from Azure Blob Storage using the default container
     *
     * @param fileName The name of the file to delete
     * @return true if the blob existed and was deleted, false otherwise
     */
    public boolean deleteFile(String fileName) {
        return deleteFile(fileName, this.containerName);
    }

    /**
     * Deletes a file from a specific Azure Blob Storage container
     *
     * @param fileName The name of the file to delete
     * @param targetContainerName The container name
     * @return true if the blob existed and was deleted, false otherwise
     */
    public boolean deleteFile(String fileName, String targetContainerName) {
        try {
            log.info("Deleting file from Azure Blob Storage container '{}': {}", targetContainerName, fileName);
            BlobContainerClient targetContainer = getContainerClient(targetContainerName);
            BlobClient blobClient = targetContainer.getBlobClient(fileName);
            boolean deleted = blobClient.deleteIfExists();
            log.info("File '{}' deletion status in container '{}': {}", fileName, targetContainerName, deleted);
            return deleted;
        } catch (Exception e) {
            log.error("Error deleting file '{}' from Azure container '{}': {}", fileName, targetContainerName, e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from Azure storage", e);
        }
    }

    private byte[] compressToTargetSize(byte[] imageBytes, int targetKB) {

        try {

            int targetBytes = targetKB * 1024;

            BufferedImage image =
                    ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (image == null) {
                return imageBytes;
            }

            // Convert PNG with transparency to RGB
            if (image.getColorModel().hasAlpha()) {

                BufferedImage rgb =
                        new BufferedImage(
                                image.getWidth(),
                                image.getHeight(),
                                BufferedImage.TYPE_INT_RGB);

                Graphics2D g = rgb.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
                g.drawImage(image, 0, 0, null);
                g.dispose();

                image = rgb;
            }

            // Resize to max width 1024
            image = resizeImage(image);

            byte[] bestImage = imageBytes;

            while (true) {

                float low = 0.10f;
                float high = 1.00f;

                byte[] candidate = null;

                // Binary Search
                while (high - low > 0.02f) {

                    float quality = (low + high) / 2;

                    byte[] compressed =
                            compressWithQuality(image, quality);

                    if (compressed.length > targetBytes) {

                        high = quality;

                    } else {

                        candidate = compressed;
                        low = quality;
                    }

                }

                if (candidate != null) {

                    bestImage = candidate;

                    if (candidate.length <= targetBytes) {
                        break;
                    }
                }

                // Still larger than target?
                // Reduce image dimensions by 10%

                int newWidth =
                        (int) (image.getWidth() * 0.9);

                int newHeight =
                        (int) (image.getHeight() * 0.9);

                if (newWidth < 300 || newHeight < 300) {
                    break;
                }

                ByteArrayOutputStream bos =
                        new ByteArrayOutputStream();

                Thumbnails.of(image)
                        .size(newWidth, newHeight)
                        .outputFormat("jpg")
                        .toOutputStream(bos);

                image = ImageIO.read(
                        new ByteArrayInputStream(
                                bos.toByteArray()));

            }

            return bestImage;

        } catch (Exception e) {

            log.error("Compression Failed", e);

            return imageBytes;

        }

    }

    private byte[] compressWithQuality(
            BufferedImage image,
            float quality) throws Exception {

        ByteArrayOutputStream bos =
                new ByteArrayOutputStream();

        ImageWriter writer =
                ImageIO.getImageWritersByFormatName("jpg").next();

        ImageWriteParam param =
                writer.getDefaultWriteParam();

        param.setCompressionMode(
                ImageWriteParam.MODE_EXPLICIT);

        param.setCompressionQuality(quality);

        ImageOutputStream ios =
                ImageIO.createImageOutputStream(bos);

        writer.setOutput(ios);

        writer.write(
                null,
                new IIOImage(image, null, null),
                param);

        ios.close();

        writer.dispose();

        return bos.toByteArray();

    }


/*
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
    }*/

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

    /**
     * Downloads a file from a specific Azure Blob Storage container as byte array.
     * Used by the image proxy endpoint to serve inspection images
     * from ercinspectionimages-uat container (which has public access disabled).
     *
     * @param fileName  The blob file name (e.g. ER_03090004_abc123.jpg)
     * @param targetContainerName  The container name to fetch from
     * @return byte array of the file content
     */
    public byte[] downloadFileFromContainer(String fileName, String targetContainerName) {
        try {
            BlobContainerClient targetContainer = getContainerClient(targetContainerName);
            BlobClient blobClient = targetContainer.getBlobClient(fileName);

            if (!blobClient.exists()) {
                throw new RuntimeException("Image not found in container '" + targetContainerName + "': " + fileName);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.download(outputStream);

            log.info("Downloaded image {} from container {} ({} bytes)",
                    fileName, targetContainerName, outputStream.size());
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error downloading image {} from container {}: {}",
                    fileName, targetContainerName, e.getMessage(), e);
            throw new RuntimeException("Failed to download image from Azure", e);
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
