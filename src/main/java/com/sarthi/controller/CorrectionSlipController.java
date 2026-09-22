package com.sarthi.controller;

import com.sarthi.dto.CorrectionSlipRequestDTO;
import com.sarthi.dto.CorrectionSlipResponseDTO;
import com.sarthi.dto.StoreCorrectionSlipRequestDTO;
import com.sarthi.dto.StoreCorrectionSlipResponseDTO;
import com.sarthi.entity.certificate.CorrectionSlipDocument;
import com.sarthi.service.CorrectionSlipService;
import com.sarthi.service.CorrectionSlipStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Correction Slip (Correction to Inspection Certificate).
 * Endpoint: /api/correction-slip
 */
@RestController
@RequestMapping("/api/correction-slip")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CorrectionSlipController {

    private final CorrectionSlipService correctionSlipService;
    private final CorrectionSlipStorageService correctionSlipStorageService;

    /**
     * GET /api/correction-slip?callNo=EP-07030001
     * Fetch all correction rows for a call number.
     */
    @GetMapping
    public ResponseEntity<?> getByCallNo(@RequestParam String callNo) {
        log.info("REST GET correction-slip for callNo: {}", callNo);
        try {
            List<CorrectionSlipResponseDTO> result = correctionSlipService.getByCallNo(callNo);
            if (result.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error fetching correction slip: {}", e.getMessage());
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching correction slip for callNo {}: ", callNo, e);
            return serverError("Failed to fetch correction slip data.");
        }
    }

    /**
     * POST /api/correction-slip
     * Save (upsert) all correction rows for a call number.
     * Replaces any previously saved rows for the same callNo.
     */
    @PostMapping
    public ResponseEntity<?> saveOrUpdate(@RequestBody CorrectionSlipRequestDTO request) {
        log.info("REST POST correction-slip for callNo: {}", request != null ? request.getCallNo() : "null");
        try {
            List<CorrectionSlipResponseDTO> saved = correctionSlipService.saveOrUpdateAll(request);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error saving correction slip: {}", e.getMessage());
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error saving correction slip: ", e);
            return serverError("Failed to save correction slip data.");
        }
    }

    /**
     * POST /api/correction-slip/compress-and-store
     * Compresses the generated Correction Slip PDF (before eSign or after signing)
     * and stores it in the Azure container 'ic-correctionslip' with the structured folder hierarchy.
     */
    @PostMapping("/compress-and-store")
    public ResponseEntity<?> compressAndStore(@RequestBody StoreCorrectionSlipRequestDTO request) {
        log.info("REST POST /api/correction-slip/compress-and-store for callNo: {}, stage: {}",
                request != null ? request.getCallNo() : "null",
                request != null ? request.getStage() : "null");
        try {
            StoreCorrectionSlipResponseDTO response = correctionSlipStorageService.compressAndStore(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Validation error storing correction slip PDF: {}", e.getMessage());
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error storing correction slip PDF: ", e);
            return serverError("Failed to compress and store correction slip PDF: " + e.getMessage());
        }
    }

    /**
     * GET /api/correction-slip/document?callNo=...
     * Get document metadata and check if a correction slip PDF exists.
     */
    @GetMapping("/document")
    public ResponseEntity<?> getDocument(@RequestParam String callNo) {
        log.info("REST GET /api/correction-slip/document for callNo: {}", callNo);
        try {
            Optional<CorrectionSlipDocument> docOpt = correctionSlipStorageService.getLatestDocument(callNo);
            if (docOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of("exists", false));
            }
            CorrectionSlipDocument doc = docOpt.get();
            Map<String, Object> res = new HashMap<>();
            res.put("exists", true);
            res.put("id", doc.getId());
            res.put("callNo", doc.getCallNo());
            res.put("icNumber", doc.getIcNumber());
            res.put("moduleType", doc.getModuleType());
            res.put("fileName", doc.getOriginalFileName());
            res.put("blobFileName", doc.getBlobFileName());
            res.put("blobUrl", doc.getBlobUrl());
            res.put("fileSizeOriginal", doc.getFileSizeOriginal());
            res.put("fileSizeCompressed", doc.getFileSizeCompressed());
            res.put("stage", doc.getStage());
            res.put("uploadedBy", doc.getUploadedBy());
            res.put("uploadedAt", doc.getUploadedAt());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Error fetching correction slip document metadata for {}: ", callNo, e);
            return serverError("Failed to fetch correction slip document metadata.");
        }
    }

    /**
     * GET /api/correction-slip/view-pdf/{*callNo}
     * View the stored correction slip PDF inline in the browser.
     */
    @GetMapping("/view-pdf/{*callNo}")
    public ResponseEntity<Resource> viewPdf(@PathVariable String callNo) {
        if (callNo != null && callNo.startsWith("/")) {
            callNo = callNo.substring(1);
        }
        log.info("REST GET /api/correction-slip/view-pdf for callNo: {}", callNo);
        return correctionSlipStorageService.viewPdf(callNo);
    }

    /**
     * GET /api/correction-slip/download-pdf/{*callNo}
     * Download the stored correction slip PDF as an attachment.
     */
    @GetMapping("/download-pdf/{*callNo}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable String callNo) {
        if (callNo != null && callNo.startsWith("/")) {
            callNo = callNo.substring(1);
        }
        log.info("REST GET /api/correction-slip/download-pdf for callNo: {}", callNo);
        return correctionSlipStorageService.downloadPdf(callNo);
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, String>> serverError(String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
