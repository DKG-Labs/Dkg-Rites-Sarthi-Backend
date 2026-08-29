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

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    /**
     * Upload an e-signed certificate PDF (Base64) to Azure Blob Storage and save metadata in DB.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCertificate(@RequestBody Map<String, String> payload) {
        String icNumber = payload.get("icNumber");
        String base64Data = payload.get("signedData");
        String fileName = payload.get("fileName");
        String uploadedBy = payload.get("uploadedBy");

        if (icNumber == null || icNumber.trim().isEmpty() || base64Data == null || base64Data.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Missing required parameters: icNumber and signedData"
            ));
        }

        try {
            String targetFileName = fileName;
            if (targetFileName == null || targetFileName.trim().isEmpty()) {
                targetFileName = icNumber.replaceAll("[/\\\\?%*:|\"<>]", "_") + ".pdf";
            }

            // 1. Upload to Azure
            String blobUrl = azureBlobStorageService.uploadBase64File(base64Data, targetFileName);

            // 2. Save/Update mapping in Database
            CertificateStorage storage = certificateStorageRepository.findByIcNumber(icNumber.trim())
                    .orElse(new CertificateStorage());

            storage.setIcNumber(icNumber.trim());
            storage.setBlobUrl(blobUrl);
            storage.setFileName(targetFileName);
            storage.setUploadedBy(uploadedBy != null ? uploadedBy : "Inspecting Engineer");
            storage.setUploadedAt(LocalDateTime.now());

            certificateStorageRepository.save(storage);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Certificate uploaded successfully to Azure and database",
                "icNumber", icNumber,
                "fileName", targetFileName,
                "url", blobUrl
            ));

        } catch (Exception e) {
            log.error("Failed to upload certificate for {}: {}", icNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Upload failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Upload an e-signed certificate PDF (Multipart File) to Azure Blob Storage and save metadata in DB.
     */
    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadCertificateFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("icNumber") String icNumber,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy) {

        if (icNumber == null || icNumber.trim().isEmpty() || file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Missing required parameters: icNumber and non-empty file"
            ));
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String targetFileName = (originalFileName != null && !originalFileName.trim().isEmpty())
                    ? originalFileName
                    : icNumber.replaceAll("[/\\\\?%*:|\"<>]", "_") + ".pdf";

            String blobUrl = azureBlobStorageService.uploadFileBytes(file.getBytes(), targetFileName);

            CertificateStorage storage = certificateStorageRepository.findByIcNumber(icNumber.trim())
                    .orElse(new CertificateStorage());

            storage.setIcNumber(icNumber.trim());
            storage.setBlobUrl(blobUrl);
            storage.setFileName(targetFileName);
            storage.setUploadedBy(uploadedBy != null ? uploadedBy : "Inspecting Engineer");
            storage.setUploadedAt(LocalDateTime.now());

            certificateStorageRepository.save(storage);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Certificate file uploaded successfully to Azure and database",
                "icNumber", icNumber,
                "fileName", targetFileName,
                "url", blobUrl
            ));

        } catch (Exception e) {
            log.error("Failed to upload certificate file for {}: {}", icNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "File upload failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Update an existing e-signed certificate PDF (Base64) in Azure Blob Storage and DB.
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateCertificatePut(@RequestBody Map<String, String> payload) {
        return performUpdate(payload);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateCertificatePost(@RequestBody Map<String, String> payload) {
        return performUpdate(payload);
    }

    private ResponseEntity<?> performUpdate(Map<String, String> payload) {
        String icNumber = payload != null ? payload.get("icNumber") : null;
        String base64Data = payload != null ? payload.get("signedData") : null;
        String newFileName = payload != null ? payload.get("fileName") : null;
        String updatedBy = payload != null ? (payload.get("uploadedBy") != null ? payload.get("uploadedBy") : payload.get("updatedBy")) : null;

        if (icNumber == null || icNumber.trim().isEmpty() || base64Data == null || base64Data.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Missing required parameters: icNumber and signedData"
            ));
        }

        try {
            Optional<CertificateStorage> storageOpt = certificateStorageRepository.findByIcNumber(icNumber.trim());
            if (storageOpt.isEmpty()) {
                storageOpt = certificateStorageRepository.findByCallNumber(icNumber.trim());
            }

            CertificateStorage storage;
            String oldFileName = null;
            if (storageOpt.isPresent()) {
                storage = storageOpt.get();
                oldFileName = storage.getFileName();
            } else {
                storage = new CertificateStorage();
                storage.setIcNumber(icNumber.trim());
            }

            String targetFileName = newFileName;
            if (targetFileName == null || targetFileName.trim().isEmpty()) {
                targetFileName = (oldFileName != null && !oldFileName.trim().isEmpty())
                        ? oldFileName
                        : icNumber.replaceAll("[/\\\\?%*:|\"<>]", "_") + ".pdf";
            }

            // If file name changed and old file exists, remove old blob from Azure
            if (oldFileName != null && !oldFileName.equals(targetFileName)) {
                try {
                    azureBlobStorageService.deleteFile(oldFileName);
                } catch (Exception e) {
                    log.warn("Could not delete old blob '{}' during update: {}", oldFileName, e.getMessage());
                }
            }

            // Upload / Overwrite in Azure
            String blobUrl = azureBlobStorageService.uploadBase64File(base64Data, targetFileName);

            storage.setBlobUrl(blobUrl);
            storage.setFileName(targetFileName);
            if (updatedBy != null && !updatedBy.trim().isEmpty()) {
                storage.setUploadedBy(updatedBy);
            }
            storage.setUploadedAt(LocalDateTime.now());

            certificateStorageRepository.save(storage);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Certificate updated successfully in Azure and database",
                "icNumber", icNumber,
                "fileName", targetFileName,
                "url", blobUrl
            ));
        } catch (Exception e) {
            log.error("Failed to update certificate for {}: {}", icNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Update failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Update an existing e-signed certificate PDF (Multipart File) in Azure Blob Storage and DB.
     */
    @PutMapping(value = "/update-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCertificateFilePut(
            @RequestParam("file") MultipartFile file,
            @RequestParam("icNumber") String icNumber,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy) {
        return performUpdateFile(file, icNumber, uploadedBy);
    }

    @PostMapping(value = "/update-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCertificateFilePost(
            @RequestParam("file") MultipartFile file,
            @RequestParam("icNumber") String icNumber,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy) {
        return performUpdateFile(file, icNumber, uploadedBy);
    }

    private ResponseEntity<?> performUpdateFile(MultipartFile file, String icNumber, String uploadedBy) {
        if (icNumber == null || icNumber.trim().isEmpty() || file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Missing required parameters: icNumber and non-empty file"
            ));
        }

        try {
            Optional<CertificateStorage> storageOpt = certificateStorageRepository.findByIcNumber(icNumber.trim());
            if (storageOpt.isEmpty()) {
                storageOpt = certificateStorageRepository.findByCallNumber(icNumber.trim());
            }

            CertificateStorage storage;
            String oldFileName = null;
            if (storageOpt.isPresent()) {
                storage = storageOpt.get();
                oldFileName = storage.getFileName();
            } else {
                storage = new CertificateStorage();
                storage.setIcNumber(icNumber.trim());
            }

            String originalFileName = file.getOriginalFilename();
            String targetFileName = (originalFileName != null && !originalFileName.trim().isEmpty())
                    ? originalFileName
                    : ((oldFileName != null && !oldFileName.trim().isEmpty())
                            ? oldFileName
                            : icNumber.replaceAll("[/\\\\?%*:|\"<>]", "_") + ".pdf");

            if (oldFileName != null && !oldFileName.equals(targetFileName)) {
                try {
                    azureBlobStorageService.deleteFile(oldFileName);
                } catch (Exception e) {
                    log.warn("Could not delete old blob '{}' during update-file: {}", oldFileName, e.getMessage());
                }
            }

            String blobUrl = azureBlobStorageService.uploadFileBytes(file.getBytes(), targetFileName);

            storage.setBlobUrl(blobUrl);
            storage.setFileName(targetFileName);
            if (uploadedBy != null && !uploadedBy.trim().isEmpty()) {
                storage.setUploadedBy(uploadedBy);
            }
            storage.setUploadedAt(LocalDateTime.now());

            certificateStorageRepository.save(storage);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Certificate file updated successfully in Azure and database",
                "icNumber", icNumber,
                "fileName", targetFileName,
                "url", blobUrl
            ));
        } catch (Exception e) {
            log.error("Failed to update certificate file for {}: {}", icNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Update file failed: " + e.getMessage()
            ));
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


    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCertificateByParam(
            @RequestParam(value = "icNumber", required = false) String icNumber,
            @RequestParam(value = "fileName", required = false) String fileName) {
        return performDelete(icNumber, fileName);
    }

    @DeleteMapping("/delete/{*icNumber}")
    public ResponseEntity<?> deleteCertificate(@PathVariable String icNumber) {
        if (icNumber != null && icNumber.startsWith("/")) {
            icNumber = icNumber.substring(1);
        }
        return performDelete(icNumber, null);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCertificateByBody(@RequestBody(required = false) Map<String, String> payload) {
        String icNumber = payload != null ? payload.get("icNumber") : null;
        String fileName = payload != null ? payload.get("fileName") : null;
        return performDelete(icNumber, fileName);
    }

    private ResponseEntity<?> performDelete(String icNumber, String fileName) {
        log.info("Request to delete certificate from Azure. icNumber: {}, fileName: {}", icNumber, fileName);

        if ((icNumber == null || icNumber.trim().isEmpty()) && (fileName == null || fileName.trim().isEmpty())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Either 'icNumber' or 'fileName' parameter must be provided"
            ));
        }

        Optional<CertificateStorage> storageOpt = Optional.empty();
        if (icNumber != null && !icNumber.trim().isEmpty()) {
            storageOpt = certificateStorageRepository.findByIcNumber(icNumber.trim());
            if (storageOpt.isEmpty()) {
                storageOpt = certificateStorageRepository.findByCallNumber(icNumber.trim());
            }
        }

        String fileToDelete = fileName;
        boolean dbRecordDeleted = false;

        if (storageOpt.isPresent()) {
            CertificateStorage storage = storageOpt.get();
            if (fileToDelete == null || fileToDelete.trim().isEmpty()) {
                fileToDelete = storage.getFileName();
            }
            certificateStorageRepository.delete(storage);
            dbRecordDeleted = true;
            log.info("Deleted CertificateStorage record for IC: {}", storage.getIcNumber());
        }

        // If fileToDelete is still not resolved, try standard sanitized name
        if ((fileToDelete == null || fileToDelete.trim().isEmpty()) && icNumber != null) {
            fileToDelete = icNumber.replaceAll("[/\\\\?%*:|\"<>]", "_") + ".pdf";
        }

        boolean blobDeleted = false;
        if (fileToDelete != null && !fileToDelete.trim().isEmpty()) {
            try {
                blobDeleted = azureBlobStorageService.deleteFile(fileToDelete);
            } catch (Exception e) {
                log.warn("Could not delete blob '{}' from Azure: {}", fileToDelete, e.getMessage());
            }
        }

        if (!dbRecordDeleted && !blobDeleted) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "No certificate found in database or Azure storage for the provided identifier",
                "icNumber", icNumber != null ? icNumber : "",
                "fileName", fileToDelete != null ? fileToDelete : ""
            ));
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Certificate deleted successfully from Azure and database",
            "icNumber", icNumber != null ? icNumber : "",
            "fileName", fileToDelete != null ? fileToDelete : "",
            "dbRecordDeleted", dbRecordDeleted,
            "blobDeleted", blobDeleted
        ));
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
