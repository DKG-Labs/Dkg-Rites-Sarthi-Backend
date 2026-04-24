package com.sarthi.controller.certificate;

import com.sarthi.entity.certificate.CertificateStorage;
import com.sarthi.repository.certificate.CertificateStorageRepository;
import com.sarthi.service.AzureBlobStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/certificate-storage")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CertificateStorageController {

    private final AzureBlobStorageService azureBlobStorageService;
    private final CertificateStorageRepository certificateStorageRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCertificate(@RequestBody Map<String, String> payload) {
        String icNumber = payload.get("icNumber");
        String base64Data = payload.get("signedData");
        String fileName = payload.get("fileName");
        String uploadedBy = payload.get("uploadedBy");

        if (icNumber == null || base64Data == null || fileName == null) {
            return ResponseEntity.badRequest().body("Missing required parameters: icNumber, signedData, or fileName");
        }

        try {
            // 1. Upload to Azure
            String blobUrl = azureBlobStorageService.uploadBase64File(base64Data, fileName);

            // 2. Save/Update mapping in Database
            CertificateStorage storage = certificateStorageRepository.findByIcNumber(icNumber)
                    .orElse(new CertificateStorage());
            
            storage.setIcNumber(icNumber);
            storage.setBlobUrl(blobUrl);
            storage.setFileName(fileName);
            storage.setUploadedBy(uploadedBy);
            
            certificateStorageRepository.save(storage);

            return ResponseEntity.ok(Map.of("message", "Certificate uploaded successfully", "url", blobUrl));
            
        } catch (Exception e) {
            log.error("Failed to upload certificate for {}: {}", icNumber, e.getMessage());
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/view/{icNumber}")
    public ResponseEntity<?> viewCertificate(@PathVariable String icNumber) {
        log.info("Fetching certificate from Azure for IC: {}", icNumber);
        
        Optional<CertificateStorage> storageOpt = certificateStorageRepository.findByIcNumber(icNumber);
        if (storageOpt.isEmpty()) {
            return ResponseEntity.status(404).body("No signed certificate found for this IC.");
        }

        try {
            CertificateStorage storage = storageOpt.get();
            String base64Data = azureBlobStorageService.downloadAsBase64(storage.getFileName());
            
            return ResponseEntity.ok(Map.of(
                "fileName", storage.getFileName(),
                "signedData", base64Data
            ));
            
        } catch (Exception e) {
            log.error("Failed to fetch certificate for {}: {}", icNumber, e.getMessage());
            return ResponseEntity.internalServerError().body("Fetch failed: " + e.getMessage());
        }
    }

    @GetMapping("/check/{icNumber}")
    public ResponseEntity<?> checkIfExists(@PathVariable String icNumber) {
        boolean exists = certificateStorageRepository.findByIcNumber(icNumber).isPresent();
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
