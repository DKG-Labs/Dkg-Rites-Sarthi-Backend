package com.sarthi.controller;

import com.sarthi.entity.InspectionImage;
import com.sarthi.repository.InspectionImageRepository;
import com.sarthi.service.AzureBlobStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to proxy inspection images from Azure Blob Storage.
 *
 * Azure Blob Storage has public access disabled, so the frontend cannot
 * fetch images directly from the blob URL. This controller acts as a
 * backend proxy: it downloads the image from Azure using the SDK
 * (which uses authenticated access) and streams the bytes to the client.
 *
 * Endpoint: GET /api/images/{imageName}
 */
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
@Slf4j
public class InspectionImageController {

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    @Autowired
    private InspectionImageRepository inspectionImageRepository;

    @Value("${azure.storage.images-container-name}")
    private String imagesContainerName;

    /**
     * Proxy endpoint to serve inspection images.
     * Downloads the image from Azure Blob Storage (using authenticated SDK access)
     * and returns it as an HTTP response so the browser can display it.
     *
     * @param imageName the blob file name (e.g. ER_03090004_abc123.jpg)
     * @return image bytes with correct content-type
     */
    @GetMapping("/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) {
        log.info("GET /api/images/{} - Fetching inspection image", imageName);
        try {
            byte[] imageBytes = azureBlobStorageService.downloadFileFromContainer(imageName, imagesContainerName);

            String contentType = "image/jpeg";
            String lowerName = imageName.toLowerCase();
            if (lowerName.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerName.endsWith(".webp")) {
                contentType = "image/webp";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageBytes.length);
            headers.set(HttpHeaders.CACHE_CONTROL, "max-age=3600, public");

            log.info("Serving image {} ({} bytes)", imageName, imageBytes.length);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Failed to fetch image {}: {}", imageName, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
