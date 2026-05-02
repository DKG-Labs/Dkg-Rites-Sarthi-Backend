package com.sarthi.service.Impl;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CrisAuthServic {

    @Value("${cris.base-url}")
    private String baseUrl;

    @Value("${cris.username}")
    private String username;

    @Value("${cris.password}")
    private String password;

    private final RestTemplate restTemplate = new RestTemplate();

    private String token; // cache in memory
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public synchronized String getToken() {

        if (token != null && !isTokenExpired(token)) {
            return token;
        }

        String url = baseUrl + "/authenticate";

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, body, Map.class);

        // CRIS returns JWT in "token"
        token = (String) response.getBody().get("token");
        System.out.println("Fetched new CRIS token.");

        return token;
    }

    private boolean isTokenExpired(String encodedToken) {
        try {
            String[] parts = encodedToken.split("\\.");
            if (parts.length < 2) return false; // not a standard JWT

            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> map = objectMapper.readValue(payload, Map.class);

            if (map.containsKey("exp")) {
                long exp = ((Number) map.get("exp")).longValue();
                // Expired if within 5 minutes of expiration
                return (exp * 1000) < (System.currentTimeMillis() + 300000);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse CRIS token expiration: " + e.getMessage());
        }
        return false;
    }
}


