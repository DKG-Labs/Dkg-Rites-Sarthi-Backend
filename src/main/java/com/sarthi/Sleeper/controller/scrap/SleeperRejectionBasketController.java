package com.sarthi.Sleeper.controller.scrap;

import com.sarthi.Sleeper.dto.scrap.SleeperScrapVerificationRequest;
import com.sarthi.Sleeper.service.scrap.SleeperRejectionBasketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Concrete Sleeper Rejection Basket, Scrap Verification and Scrap Ledger.
 * The user (and so which plants they may see / scrap for) always comes from the login token.
 * Photos are served through the existing GET /api/scrap-entry/image-proxy?url=...
 */
@RestController
@RequestMapping("/api/sleeper-scrap")
@CrossOrigin(origins = "*")
public class SleeperRejectionBasketController {

    @Autowired
    private SleeperRejectionBasketService service;

    /** Plant-wise summary: one row per Manufacturer - Plant */
    @GetMapping("/basket")
    public ResponseEntity<List<Map<String, Object>>> getBasket(@AuthenticationPrincipal Object principal) {
        return ResponseEntity.ok(service.getBasket(principal));
    }

    /** Pending sleepers of one plant (rejected, not yet scrapped), newest first */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPending(@AuthenticationPrincipal Object principal,
                                                          @RequestParam String plantId) {
        return ResponseEntity.ok(service.getPending(principal, plantId));
    }

    /** Scrap ledger (passbook) with running pending balance (txnType = REJECTION / SCRAP) */
    @GetMapping("/ledger")
    public ResponseEntity<Map<String, Object>> getLedger(
            @AuthenticationPrincipal Object principal,
            @RequestParam String plantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String txnType,
            @RequestParam(required = false) String batchNo,
            @RequestParam(required = false) String sleeperId) {
        return ResponseEntity.ok(service.getLedger(principal, plantId, fromDate, toDate, txnType, batchNo, sleeperId));
    }

    /** Full scrap verification record: sleepers, photos, declaration, audit trail */
    @GetMapping("/verification/{id}")
    public ResponseEntity<Map<String, Object>> getVerification(@AuthenticationPrincipal Object principal,
                                                               @PathVariable Long id) {
        return ResponseEntity.ok(service.getVerification(principal, id));
    }

    @PostMapping("/verification")
    public ResponseEntity<Map<String, Object>> addVerification(@AuthenticationPrincipal Object principal,
                                                               @RequestBody SleeperScrapVerificationRequest request) {
        return ResponseEntity.ok(service.addVerification(principal, request));
    }

    /** Edit within 1 hour of recording (reason required) */
    @PutMapping("/verification/{id}")
    public ResponseEntity<Map<String, Object>> updateVerification(@AuthenticationPrincipal Object principal,
                                                                  @PathVariable Long id,
                                                                  @RequestBody SleeperScrapVerificationRequest request) {
        return ResponseEntity.ok(service.updateVerification(principal, id, request));
    }

    /** Delete within 1 hour of recording (reason required); kept as DELETED, sleepers return to the basket */
    @DeleteMapping("/verification/{id}")
    public ResponseEntity<Map<String, Object>> deleteVerification(@AuthenticationPrincipal Object principal,
                                                                  @PathVariable Long id,
                                                                  @RequestParam String reason) {
        service.deleteVerification(principal, id, reason);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
