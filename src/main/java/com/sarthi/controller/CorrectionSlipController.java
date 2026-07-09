package com.sarthi.controller;

import com.sarthi.dto.CorrectionSlipRequestDTO;
import com.sarthi.dto.CorrectionSlipResponseDTO;
import com.sarthi.service.CorrectionSlipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     *
     * Request body example:
     * {
     *   "callNo": "EP-07030001",
     *   "createdBy": "104392",
     *   "rows": [
     *     { "columnName": "certificateNo", "readAs": "NEW VALUE", "insteadOf": "OLD VALUE" }
     *   ]
     * }
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
