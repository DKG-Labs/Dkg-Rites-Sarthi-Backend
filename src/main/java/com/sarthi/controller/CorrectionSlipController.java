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

import java.util.ArrayList;
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
     * Get latest document metadata and check if a correction slip PDF exists.
     */
    @GetMapping("/document")
    public ResponseEntity<?> getDocument(@RequestParam String callNo) {
        log.info("REST GET /api/correction-slip/document for callNo: {}", callNo);
        try {
            Optional<CorrectionSlipDocument> docOpt = correctionSlipStorageService.getLatestDocument(callNo);
            if (docOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of("exists", false, "totalCount", 0));
            }
            List<CorrectionSlipDocument> allDocs = correctionSlipStorageService.getAllDocuments(callNo);
            CorrectionSlipDocument doc = docOpt.get();
            Map<String, Object> res = new HashMap<>();
            res.put("exists", true);
            res.put("totalCount", allDocs.size());
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
     * GET /api/correction-slip/documents?callNo=...
     * Get all stored correction slip documents for a call number with sequence indexing.
     */
    @GetMapping("/documents")
    public ResponseEntity<?> getAllDocuments(@RequestParam String callNo) {
        log.info("REST GET /api/correction-slip/documents for callNo: {}", callNo);
        try {
            List<CorrectionSlipDocument> allDocs = correctionSlipStorageService.getAllDocuments(callNo);
            List<Map<String, Object>> resList = new ArrayList<>();
            for (int i = 0; i < allDocs.size(); i++) {
                CorrectionSlipDocument doc = allDocs.get(i);
                Map<String, Object> item = new HashMap<>();
                item.put("id", doc.getId());
                item.put("slipNumber", i + 1);
                item.put("title", "Correction Slip " + (i + 1));
                item.put("callNo", doc.getCallNo());
                item.put("icNumber", doc.getIcNumber());
                item.put("moduleType", doc.getModuleType());
                item.put("fileName", doc.getOriginalFileName());
                item.put("blobFileName", doc.getBlobFileName());
                item.put("blobUrl", doc.getBlobUrl());
                item.put("fileSizeOriginal", doc.getFileSizeOriginal());
                item.put("fileSizeCompressed", doc.getFileSizeCompressed());
                item.put("stage", doc.getStage());
                item.put("uploadedBy", doc.getUploadedBy());
                item.put("uploadedAt", doc.getUploadedAt());
                resList.add(item);
            }
            return ResponseEntity.ok(resList);
        } catch (Exception e) {
            log.error("Error fetching all correction slip documents for {}: ", callNo, e);
            return serverError("Failed to fetch correction slip documents list.");
        }
    }

    /**
     * GET /api/correction-slip/view-pdf-by-id/{id}
     * View a specific stored correction slip PDF inline.
     */
    @GetMapping("/view-pdf-by-id/{id}")
    public ResponseEntity<Resource> viewPdfById(@PathVariable Long id) {
        log.info("REST GET /api/correction-slip/view-pdf-by-id for id: {}", id);
        try {
            return correctionSlipStorageService.viewPdfById(id);
        } catch (Exception e) {
            log.error("Error viewing correction slip PDF for id {}: ", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/correction-slip/download-pdf-by-id/{id}
     * Download a specific stored correction slip PDF.
     */
    @GetMapping("/download-pdf-by-id/{id}")
    public ResponseEntity<Resource> downloadPdfById(@PathVariable Long id) {
        log.info("REST GET /api/correction-slip/download-pdf-by-id for id: {}", id);
        try {
            return correctionSlipStorageService.downloadPdfById(id);
        } catch (Exception e) {
            log.error("Error downloading correction slip PDF for id {}: ", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/correction-slip/view-pdf/{callNo}
     * View the latest stored correction slip PDF inline in the browser.
     */
    @GetMapping({"/view-pdf/{callNo}", "/view-pdf/{*callNo}"})
    public ResponseEntity<Resource> viewPdf(@PathVariable(required = false) String callNo, jakarta.servlet.http.HttpServletRequest request) {
        if (callNo == null || callNo.trim().isEmpty()) {
            String uri = request != null ? request.getRequestURI() : "";
            int idx = uri.indexOf("/view-pdf/");
            if (idx != -1) {
                callNo = uri.substring(idx + "/view-pdf/".length());
            }
        }
        if (callNo != null && callNo.startsWith("/")) {
            callNo = callNo.substring(1);
        }
        log.info("REST GET /api/correction-slip/view-pdf for callNo: {}", callNo);
        try {
            return correctionSlipStorageService.viewPdf(callNo);
        } catch (Exception e) {
            log.error("Error viewing correction slip PDF for callNo {}: ", callNo, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/correction-slip/download-pdf/{callNo}
     * Download the latest stored correction slip PDF as an attachment.
     */
    @GetMapping({"/download-pdf/{callNo}", "/download-pdf/{*callNo}"})
    public ResponseEntity<Resource> downloadPdf(@PathVariable(required = false) String callNo, jakarta.servlet.http.HttpServletRequest request) {
        if (callNo == null || callNo.trim().isEmpty()) {
            String uri = request != null ? request.getRequestURI() : "";
            int idx = uri.indexOf("/download-pdf/");
            if (idx != -1) {
                callNo = uri.substring(idx + "/download-pdf/".length());
            }
        }
        if (callNo != null && callNo.startsWith("/")) {
            callNo = callNo.substring(1);
        }
        log.info("REST GET /api/correction-slip/download-pdf for callNo: {}", callNo);
        try {
            return correctionSlipStorageService.downloadPdf(callNo);
        } catch (Exception e) {
            log.error("Error downloading correction slip PDF for callNo {}: ", callNo, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/correction-slip?callNo=...
     * Deletes the correction slip for a call (both DB entries and stored documents).
     */
    @DeleteMapping
    public ResponseEntity<?> deleteCorrectionSlip(@RequestParam String callNo) {
        log.info("REST DELETE /api/correction-slip for callNo: {}", callNo);
        try {
            if (callNo == null || callNo.trim().isEmpty()) {
                return badRequest("Call number is required.");
            }
            String clean = callNo.trim();
            correctionSlipService.deleteByCallNo(clean);
            correctionSlipStorageService.deleteCorrectionSlip(clean);
            return ResponseEntity.ok(Map.of("success", true, "message", "Correction slip deleted successfully", "callNo", clean));
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting correction slip for callNo {}: ", callNo, e);
            return serverError("Failed to delete correction slip: " + e.getMessage());
        }
    }

    @DeleteMapping("/{*callNo}")
    public ResponseEntity<?> deleteCorrectionSlipByPath(@PathVariable String callNo) {
        if (callNo != null && callNo.startsWith("/")) {
            callNo = callNo.substring(1);
        }
        return deleteCorrectionSlip(callNo);
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
