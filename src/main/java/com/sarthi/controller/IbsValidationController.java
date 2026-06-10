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
public class IbsValidationController {

    @PostMapping("/validate-book-set")
    public ResponseEntity<?> validateBookSet(@RequestBody Map<String, Object> payload) {
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
