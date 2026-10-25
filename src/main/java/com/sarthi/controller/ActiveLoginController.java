package com.sarthi.controller;

import com.sarthi.entity.mfa.AuthenticatorVerification;
import com.sarthi.repository.mfa.AuthenticatorVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/active-logins")
@CrossOrigin(origins = "*")
public class ActiveLoginController {

    private static final String AUTHENTICATED = "AUTHENTICATED";

    @Autowired
    private AuthenticatorVerificationRepository verificationRepository;

    /**
     * GET /api/active-logins
     * All rows of sarthi_authenticator_verifications with status = AUTHENTICATED.
     * The selfie (LONGTEXT) is left out of the list; fetch it via /{id}/selfie.
     */
    @GetMapping
    public ResponseEntity<?> getActiveLogins() {
        try {
            List<Map<String, Object>> rows = verificationRepository
                    .findByStatusOrderByVerifiedAtDesc(AUTHENTICATED)
                    .stream()
                    .map(this::toRow)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch active logins: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/{id}/selfie")
    public ResponseEntity<?> getSelfie(@PathVariable Long id) {
        return verificationRepository.findById(id)
                .map(v -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("selfieImageUrl", v.getSelfieImageUrl());
                    return ResponseEntity.ok((Object) body);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Map<String, Object> toRow(AuthenticatorVerification v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("sessionId", v.getSessionId());
        m.put("userId", v.getUserId());
        m.put("userName", v.getUserName());
        m.put("loginTransactionId", v.getLoginTransactionId());
        m.put("latitude", v.getLatitude());
        m.put("longitude", v.getLongitude());
        m.put("accuracyMeters", v.getAccuracyMeters());
        m.put("locationAddress", v.getLocationAddress());
        m.put("hasSelfie", v.getSelfieImageUrl() != null && !v.getSelfieImageUrl().isEmpty());
        m.put("deviceInfo", v.getDeviceInfo());
        m.put("ipAddress", v.getIpAddress());
        m.put("status", v.getStatus());
        m.put("createdAt", v.getCreatedAt());
        m.put("verifiedAt", v.getVerifiedAt());
        m.put("expiresAt", v.getExpiresAt());
        return m;
    }
}
