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

    /**
     * Fetch all captured inspection images for a given call.
     */
    @GetMapping("/call/{callNo}")
    public ResponseEntity<java.util.List<com.sarthi.dto.ImageCaptureDto>> getImagesForCall(
            @PathVariable String callNo,
            @RequestParam(required = false) String typeOfCall) {
        log.info("GET /api/images/call/{} typeOfCall: {}", callNo, typeOfCall);
        try {
            java.util.List<InspectionImage> images;
            if (typeOfCall != null && !typeOfCall.trim().isEmpty()) {
                images = inspectionImageRepository.findByInspectionCallNoAndTypeOfCall(callNo, typeOfCall);
            } else {
                images = inspectionImageRepository.findByInspectionCallNo(callNo);
            }

            java.util.List<com.sarthi.dto.ImageCaptureDto> dtos = new java.util.ArrayList<>();
            if (images != null) {
                for (InspectionImage img : images) {
                    com.sarthi.dto.ImageCaptureDto dto = new com.sarthi.dto.ImageCaptureDto();
                    dto.setBase64Data("/api/images/" + img.getImageName());
                    dto.setPreview("/api/images/" + img.getImageName());
                    dto.setLatitude(img.getLatitude());
                    dto.setLongitude(img.getLongitude());
                    dto.setTimestamp(img.getCreatedAt() != null ? img.getCreatedAt().toString() : null);
                    dtos.add(dto);
                }
            }
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Failed to fetch images for call {}: {}", callNo, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Save captured images for a call (uploads new images to Azure Blob Storage, retains existing, deletes removed).
     */
    @PostMapping("/call/{callNo}")
    public ResponseEntity<java.util.Map<String, Object>> saveImagesForCall(
            @PathVariable String callNo,
            @RequestBody com.sarthi.dto.SaveImagesRequestDto request) {
        log.info("POST /api/images/call/{} type: {}", callNo, request.getTypeOfCall());
        try {
            String typeOfCall = request.getTypeOfCall() != null ? request.getTypeOfCall() : "RAILPAD";
            java.util.List<com.sarthi.dto.ImageCaptureDto> images = request.getCapturedImages() != null ? request.getCapturedImages() : java.util.Collections.emptyList();

            java.util.Set<String> existingImageNames = new java.util.HashSet<>();
            java.util.List<com.sarthi.dto.ImageCaptureDto> newImages = new java.util.ArrayList<>();

            for (com.sarthi.dto.ImageCaptureDto imageDto : images) {
                if (imageDto.getBase64Data() != null && !imageDto.getBase64Data().isEmpty()) {
                    if (imageDto.getBase64Data().startsWith("/api/images/")) {
                        String existingName = imageDto.getBase64Data().substring("/api/images/".length());
                        existingImageNames.add(existingName);
                    } else {
                        newImages.add(imageDto);
                    }
                }
            }

            // Delete removed images
            java.util.List<InspectionImage> currentDbImages = inspectionImageRepository.findByInspectionCallNoAndTypeOfCall(callNo, typeOfCall);
            for (InspectionImage dbImage : currentDbImages) {
                if (!existingImageNames.contains(dbImage.getImageName())) {
                    inspectionImageRepository.delete(dbImage);
                    log.info("Deleted removed image: {}", dbImage.getImageName());
                }
            }

            // Upload and save new images
            for (com.sarthi.dto.ImageCaptureDto imageDto : newImages) {
                String fileName = callNo.replaceAll("[^a-zA-Z0-9]", "_") + "_" + java.util.UUID.randomUUID().toString() + ".jpg";
                String imageUrl = azureBlobStorageService.uploadBase64File(imageDto.getBase64Data(), fileName, imagesContainerName);

                InspectionImage imageEntity = new InspectionImage();
                imageEntity.setInspectionCallNo(callNo);
                imageEntity.setTypeOfCall(typeOfCall);
                imageEntity.setImageName(fileName);
                imageEntity.setImageUrl(imageUrl);
                imageEntity.setLatitude(imageDto.getLatitude());
                imageEntity.setLongitude(imageDto.getLongitude());
                imageEntity.setShift(request.getShift());
                imageEntity.setDateOfInspection(request.getDateOfInspection());
                imageEntity.setCreatedBy(request.getUserId());
                imageEntity.setUpdatedBy(request.getUserId());

                inspectionImageRepository.save(imageEntity);
            }

            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("success", true);
            resp.put("message", "Images saved successfully");
            resp.put("totalImages", existingImageNames.size() + newImages.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Failed to save images for call {}: {}", callNo, e.getMessage(), e);
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
}
