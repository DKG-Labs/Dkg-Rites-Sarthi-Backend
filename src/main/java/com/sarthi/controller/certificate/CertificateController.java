package com.sarthi.controller.certificate;

import com.sarthi.dto.certificate.RawMaterialCertificateDto;
import com.sarthi.dto.certificate.ProcessMaterialCertificateDto;
import com.sarthi.dto.certificate.FinalCertificateDto;
import com.sarthi.dto.certificate.IcReportDataResponse;
import com.sarthi.service.certificate.CertificateService;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Certificate Generation APIs.
 * Provides endpoints to generate inspection certificates.
 */
@RestController
@RequestMapping("/api/certificate")
@CrossOrigin(origins = "*")
public class CertificateController {

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

    @Autowired
    private CertificateService certificateService;

    /**
     * Generate Raw Material Inspection Certificate by IC Number (Query Parameter)
     *
     * @param icNumber - Inspection Call Number (e.g., RM-IC-1767772023499)
     * @return RawMaterialCertificateDto with all certificate data
     */
    @GetMapping("/raw-material")
    public ResponseEntity<?> generateRawMaterialCertificateByQuery(@RequestParam String icNumber) {
        try {
            logger.info("Generating Raw Material Certificate for IC Number (query param): {}", icNumber);
            RawMaterialCertificateDto certificate = certificateService.generateRawMaterialCertificate(icNumber);
            return ResponseEntity.ok(certificate);
        } catch (IllegalArgumentException e) {
            logger.error("Error generating certificate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating certificate: " + e.getMessage());
        }
    }

    /**
     * Generate Raw Material Inspection Certificate by IC Number (Path Variable)
     */
    @GetMapping("/raw-material/{icNumber}")
    public ResponseEntity<?> generateRawMaterialCertificate(@PathVariable String icNumber) {
        try {
            logger.info("Generating Raw Material Certificate for IC Number (path variable): {}", icNumber);
            RawMaterialCertificateDto certificate = certificateService.generateRawMaterialCertificate(icNumber);
            return ResponseEntity.ok(certificate);
        } catch (IllegalArgumentException e) {
            logger.error("Error generating certificate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating certificate: " + e.getMessage());
        }
    }

    /**
     * Generate Raw Material Inspection Certificate by Call ID
     */
    @GetMapping("/raw-material/by-id/{callId}")
    public ResponseEntity<?> generateRawMaterialCertificateById(@PathVariable Long callId) {
        try {
            logger.info("Generating Raw Material Certificate for Call ID: {}", callId);
            RawMaterialCertificateDto certificate = certificateService.generateRawMaterialCertificateById(callId);
            return ResponseEntity.ok(certificate);
        } catch (IllegalArgumentException e) {
            logger.error("Error generating certificate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating certificate: " + e.getMessage());
        }
    }

    /**
     * Generate Process Material Inspection Certificate by IC Number
     */
    @GetMapping("/process-material")
    public ResponseEntity<?> generateProcessMaterialCertificateByQuery(@RequestParam String icNumber) {
        try {
            logger.info("Generating Process Material Certificate for IC Number: {}", icNumber);
            ProcessMaterialCertificateDto certificate = certificateService.generateProcessMaterialCertificate(icNumber);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            logger.error("Error generating process certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Generate Process Material Inspection Certificate by Call ID
     */
    @GetMapping("/process-material/by-id/{callId}")
    public ResponseEntity<?> generateProcessMaterialCertificateById(@PathVariable Long callId) {
        try {
            logger.info("Generating Process Material Certificate for Call ID: {}", callId);
            ProcessMaterialCertificateDto certificate = certificateService.generateProcessMaterialCertificateById(callId);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            logger.error("Error generating process certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Generate Final Material Inspection Certificate by IC Number
     */
    @GetMapping("/final-product")
    public ResponseEntity<?> generateFinalCertificateByQuery(@RequestParam String icNumber) {
        try {
            logger.info("Generating Final Material Certificate for IC Number: {}", icNumber);
            FinalCertificateDto certificate = certificateService.generateFinalCertificate(icNumber);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            logger.error("Error generating final certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Generate Final Material Inspection Certificate by Call ID
     */
    @GetMapping("/final-product/by-id/{callId}")
    public ResponseEntity<?> generateFinalCertificateById(@PathVariable Long callId) {
        try {
            logger.info("Generating Final Material Certificate for Call ID: {}", callId);
            FinalCertificateDto certificate = certificateService.generateFinalCertificateById(callId);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            logger.error("Error generating final certificate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Generate IC Report Data with optional digital signature flag.
     * Used for E-Sign workflow.
     * 
     * @param payload - Map of request body parameters
     * @return IcReportDataResponse with status and report data
     */
    @PostMapping("/report-data")
    public ResponseEntity<IcReportDataResponse> getReportData(@RequestBody Map<String, String> payload) {
        try {
            logger.info("Generating report data for E-Sign with payload: {}", payload);
            IcReportDataResponse response = certificateService.generateReportData(payload);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating report data for e-sign", e);
            IcReportDataResponse errorResponse = IcReportDataResponse.builder()
                    .status("0")
                    .responseText("Error generating report data: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Certificate Service is UP");
    }
}
