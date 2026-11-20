package com.sarthi.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/ibs-validation")
@CrossOrigin(origins = "*", maxAge = 3600)
@lombok.RequiredArgsConstructor
public class IbsValidationController {

    private final com.sarthi.bookset.BookSetUsageGuard bookSetGuard;

    @PostMapping("/validate-book-set")
    public ResponseEntity<?> validateBookSet(@RequestBody Map<String, Object> payload) {
        // Block a Book No + Set No already used on a different IC (any user), before asking IBS.
        Object ic = payload.remove("IC_NO");
        var conflict = bookSetGuard.findConflict(
                payload.get("BK_NO") == null ? null : String.valueOf(payload.get("BK_NO")),
                payload.get("SET_NO") == null ? null : String.valueOf(payload.get("SET_NO")),
                ic == null ? null : String.valueOf(ic));
        if (conflict.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "resultFlag", 0,
                    "message", com.sarthi.bookset.BookSetUsageGuard.conflictMessage(conflict.get())));
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://ritesinsp.com/IBS2MobileAPI/api/BookSetValidation/validate";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"resultFlag\": 0, \"message\": \"Error connecting to RITES API: " + e.getMessage() + "\"}");
        }
    }
}
