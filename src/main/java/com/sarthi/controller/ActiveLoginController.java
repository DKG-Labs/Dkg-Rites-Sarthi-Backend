package com.sarthi.controller;

import com.sarthi.repository.mfa.AuthenticatorVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/active-logins")
@CrossOrigin(origins = "*")
public class ActiveLoginController {

    private static final String AUTHENTICATED = "AUTHENTICATED";
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private AuthenticatorVerificationRepository verificationRepository;

    /**
     * GET /api/active-logins
     * Rows of sarthi_authenticator_verifications with status = AUTHENTICATED that have not expired, newest first.
     * Optional params: page (0-based, default 0), size (default 10, max 100), search (name).
     * The selfie (LONGTEXT) is left out; fetch it via /{id}/selfie.
     */
    @GetMapping
    public ResponseEntity<?> getActiveLogins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        try {
            int safePage = Math.max(page, 0);
            int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
            String term = search == null ? "" : search.trim();

            Page<AuthenticatorVerificationRepository.ActiveLoginRow> result = verificationRepository
                    .findActiveLogins(AUTHENTICATED, LocalDateTime.now(), term, PageRequest.of(safePage, safeSize));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("content", result.getContent());
            body.put("page", result.getNumber());
            body.put("size", result.getSize());
            body.put("totalElements", result.getTotalElements());
            body.put("totalPages", result.getTotalPages());
            return ResponseEntity.ok(body);
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
}
