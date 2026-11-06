package com.sarthi.controller;

import com.sarthi.entity.UserMaster;
import com.sarthi.repository.mfa.AuthenticatorVerificationRepository;
import com.sarthi.service.AzureBlobStorageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/active-logins")
@CrossOrigin(origins = "*")
public class ActiveLoginController {

    private static final String AUTHENTICATED = "AUTHENTICATED";
    private static final String LOGGED_OUT = "LOGGED_OUT";
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private AuthenticatorVerificationRepository verificationRepository;

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    @Value("${azure.storage.authenticator-selfies-container-name:authenticator-selfies}")
    private String selfiesContainerName;

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
            String term = search == null ? "" : search.trim().toLowerCase();

            // One cheap query for valid logins (newest first), then keep the latest row per user
            Map<String, AuthenticatorVerificationRepository.ActiveLoginRow> latestPerUser = new LinkedHashMap<>();
            for (AuthenticatorVerificationRepository.ActiveLoginRow r :
                    verificationRepository.findAllActive(AUTHENTICATED, LocalDateTime.now())) {
                if (!term.isEmpty() && (r.getUserName() == null || !r.getUserName().toLowerCase().contains(term))) {
                    continue;
                }
                latestPerUser.putIfAbsent(String.valueOf(r.getUserId()), r);
            }

            List<AuthenticatorVerificationRepository.ActiveLoginRow> rows = new ArrayList<>(latestPerUser.values());
            rows.sort(Comparator.comparing(AuthenticatorVerificationRepository.ActiveLoginRow::getVerifiedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));

            int total = rows.size();
            int from = Math.min(safePage * safeSize, total);
            int to = Math.min(from + safeSize, total);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("content", rows.subList(from, to).stream().map(ActiveLoginController::toJson).toList());
            body.put("page", safePage);
            body.put("size", safeSize);
            body.put("totalElements", total);
            body.put("totalPages", (int) Math.ceil(total / (double) safeSize));
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch active logins: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Times are saved with LocalDateTime.now() in this server's time zone, so attach that zone
     * (e.g. "2026-11-03T13:49:43+05:30") and the browser can show them correctly in IST.
     */
    private static String withZone(LocalDateTime t) {
        return t == null ? null : t.atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime().toString();
    }

    private static Map<String, Object> toJson(AuthenticatorVerificationRepository.ActiveLoginRow r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("sessionId", r.getSessionId());
        m.put("userId", r.getUserId());
        m.put("userName", r.getUserName());
        m.put("loginTransactionId", r.getLoginTransactionId());
        m.put("latitude", r.getLatitude());
        m.put("longitude", r.getLongitude());
        m.put("accuracyMeters", r.getAccuracyMeters());
        m.put("locationAddress", r.getLocationAddress());
        m.put("hasSelfie", r.getHasSelfie());
        m.put("deviceInfo", r.getDeviceInfo());
        m.put("ipAddress", r.getIpAddress());
        m.put("status", r.getStatus());
        m.put("createdAt", withZone(r.getCreatedAt()));
        m.put("verifiedAt", withZone(r.getVerifiedAt()));
        m.put("expiresAt", withZone(r.getExpiresAt()));
        return m;
    }

    /**
     * POST /api/active-logins/logout
     * Marks the calling user's active sessions as LOGGED_OUT so they drop off the Active Login list.
     * The user is taken from the JWT; the next login will ask for the QR/selfie again.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Object principal) {
        if (!(principal instanceof UserMaster user) || user.getUserId() == null) {
            return ResponseEntity.ok(Map.of("ended", 0));
        }
        // The login page may have stored the numeric id or the login id, so match all of them
        java.util.Set<String> ids = new java.util.HashSet<>();
        ids.add(user.getUserId().toString());
        if (user.getEmployeeCode() != null) ids.add(user.getEmployeeCode());
        if (user.getUsername() != null) ids.add(user.getUsername());
        int ended = verificationRepository.endActiveSessions(ids, AUTHENTICATED, LOGGED_OUT, LocalDateTime.now());
        return ResponseEntity.ok(Map.of("ended", ended));
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

    /**
     * GET /api/active-logins/{id}/selfie/image
     * Proxies the selfie image bytes from the private Azure Blob container so the
     * browser can display it without a 403 / CORS error.
     */
    @GetMapping("/{id}/selfie/image")
    public ResponseEntity<byte[]> getSelfieImage(@PathVariable Long id) {
        return verificationRepository.findById(id)
                .map(v -> {
                    String url = v.getSelfieImageUrl();
                    if (url == null || url.isBlank()) {
                        return ResponseEntity.notFound().<byte[]>build();
                    }
                    try {
                        java.net.URI uri = new java.net.URI(url);
                        String path = uri.getPath(); // e.g. /authenticator-selfies/selfie_116_xxxx.jpg
                        // Strip leading /<container>/ to get just the blob name
                        String blobName = java.net.URLDecoder.decode(
                                path.replaceFirst("^/[^/]+/", ""), "UTF-8");
                        byte[] bytes = azureBlobStorageService.downloadFileFromContainer(
                                blobName, selfiesContainerName);
                        String contentType = blobName.toLowerCase().endsWith(".png")
                                ? "image/png" : "image/jpeg";
                        return ResponseEntity.ok()
                                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600, private")
                                .contentType(MediaType.parseMediaType(contentType))
                                .body(bytes);
                    } catch (Exception e) {
                        return ResponseEntity.notFound().<byte[]>build();
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
