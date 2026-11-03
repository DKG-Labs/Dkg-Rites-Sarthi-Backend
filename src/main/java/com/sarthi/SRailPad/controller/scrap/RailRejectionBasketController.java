package com.sarthi.SRailPad.controller.scrap;

import com.sarthi.SRailPad.dto.scrap.RailScrapVerificationRequest;
import com.sarthi.SRailPad.service.scrap.RailRejectionBasketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Rail Pad Rejection Basket, Scrap Verification and Scrap Ledger.
 * The user (and so which plants they may see / scrap for) always comes from the login token.
 * Photos are served through the existing GET /api/scrap-entry/image-proxy?url=...
 */
@RestController
@RequestMapping("/api/railpad-scrap")
@CrossOrigin(origins = "*")
public class RailRejectionBasketController {

    @Autowired
    private RailRejectionBasketService service;

    /** Plant-wise summary: one row per Manufacturer - Plant - Product Type */
    @GetMapping("/basket")
    public ResponseEntity<List<Map<String, Object>>> getBasket(
            @AuthenticationPrincipal Object principal,
            @RequestParam(required = false) String vendorCode,
            @RequestParam(required = false) String plantId,
            @RequestParam(required = false) String productType) {
        return ResponseEntity.ok(service.getBasket(principal, vendorCode, plantId, productType));
    }

    /** Rejection history of one plant + product type (stage = PROCESS / FINAL) */
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @AuthenticationPrincipal Object principal,
            @RequestParam String plantId,
            @RequestParam String productType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String stage) {
        return ResponseEntity.ok(service.getRejectionHistory(principal, plantId, productType, fromDate, toDate, stage));
    }

    /** Scrap ledger (passbook) with running pending balance (txnType = REJECTION / SCRAP) */
    @GetMapping("/ledger")
    public ResponseEntity<Map<String, Object>> getLedger(
            @AuthenticationPrincipal Object principal,
            @RequestParam String plantId,
            @RequestParam String productType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String txnType) {
        return ResponseEntity.ok(service.getLedger(principal, plantId, productType, fromDate, toDate, txnType));
    }

    /** Available pending scrap quantity (shown on the Scrap Verification form) */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPending(
            @AuthenticationPrincipal Object principal,
            @RequestParam String plantId,
            @RequestParam String productType) {
        return ResponseEntity.ok(service.getPending(principal, plantId, productType));
    }

    /** Full scrap verification record: photos, declaration, audit trail */
    @GetMapping("/verification/{id}")
    public ResponseEntity<Map<String, Object>> getVerification(@AuthenticationPrincipal Object principal,
                                                               @PathVariable Long id) {
        return ResponseEntity.ok(service.getVerification(principal, id));
    }

    @PostMapping("/verification")
    public ResponseEntity<Map<String, Object>> addVerification(@AuthenticationPrincipal Object principal,
                                                               @RequestBody RailScrapVerificationRequest request) {
        return ResponseEntity.ok(service.addVerification(principal, request));
    }

    /** Edit within 1 hour of recording (reason required) */
    @PutMapping("/verification/{id}")
    public ResponseEntity<Map<String, Object>> updateVerification(@AuthenticationPrincipal Object principal,
                                                                  @PathVariable Long id,
                                                                  @RequestBody RailScrapVerificationRequest request) {
        return ResponseEntity.ok(service.updateVerification(principal, id, request));
    }

    /** Delete within 1 hour of recording (reason required); kept as DELETED for the audit trail */
    @DeleteMapping("/verification/{id}")
    public ResponseEntity<Map<String, Object>> deleteVerification(@AuthenticationPrincipal Object principal,
                                                                  @PathVariable Long id,
                                                                  @RequestParam String reason) {
        service.deleteVerification(principal, id, reason);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
