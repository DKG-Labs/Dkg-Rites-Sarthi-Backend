package com.sarthi.controller.certificate;

import com.sarthi.entity.certificate.CertificateStorage;
import com.sarthi.repository.certificate.CertificateStorageRepository;
import com.sarthi.service.AzureBlobStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

    @GetMapping("/view")
    public ResponseEntity<?> viewCertificateByParam(@RequestParam("icNumber") String icNumber) {
        return getCertificateResponse(icNumber);
    }

    @GetMapping("/view/{*icNumber}")
    public ResponseEntity<?> viewCertificate(@PathVariable String icNumber) {
        if (icNumber != null && icNumber.startsWith("/")) {
            icNumber = icNumber.substring(1);
        }
        return getCertificateResponse(icNumber);
    }

    private ResponseEntity<?> getCertificateResponse(String icNumber) {
        log.info("Fetching certificate from Azure for IC: {}", icNumber);
        
        Optional<CertificateStorage> storageOpt = certificateStorageRepository.findByIcNumber(icNumber);
        if (storageOpt.isEmpty()) {
            storageOpt = certificateStorageRepository.findByCallNumber(icNumber);
        }
        
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

    @GetMapping("/check")
    public ResponseEntity<?> checkIfExistsByParam(@RequestParam("icNumber") String icNumber) {
        return getCheckResponse(icNumber);
    }

    @GetMapping("/check/{*icNumber}")
    public ResponseEntity<?> checkIfExists(@PathVariable String icNumber) {
        if (icNumber != null && icNumber.startsWith("/")) {
            icNumber = icNumber.substring(1);
        }
        return getCheckResponse(icNumber);
    }

    private ResponseEntity<?> getCheckResponse(String icNumber) {
        Optional<CertificateStorage> storageOpt = certificateStorageRepository.findByIcNumber(icNumber);
        if (storageOpt.isEmpty()) {
            storageOpt = certificateStorageRepository.findByCallNumber(icNumber);
        }
        return ResponseEntity.ok(Map.of("exists", storageOpt.isPresent()));
    }


    @GetMapping("/path/{callNumber}")
    public ResponseEntity<String> getCertificatePath(
            @PathVariable String callNumber,
            HttpServletRequest request) {

        String baseUrl =
                request.getScheme() +
                        "://" +
                        request.getServerName() +
                        ":" +
                        request.getServerPort()
                        + "/" + "sarthi-backend";


        String certificatePath =
                baseUrl +
                        "/api/certificate-storage/view/" +
                        callNumber +
                        ".pdf";

        return ResponseEntity.ok(certificatePath);
    }


    @GetMapping(
            value = "/view/{callNumber}.pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> openCertificate(
            @PathVariable String callNumber) {

        return azureBlobStorageService.openCertificate(
                callNumber
        );
    }
}
