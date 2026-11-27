package com.sarthi.service.Impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SmsService {

    @Value("${xpertsms.api.url}")
    private String smsApiUrl;

    @Value("${xpertsms.api.key}")
    private String smsApiKey;

    @Value("${xpertsms.sender}")
    private String sender;

    // NEW: SMS template ID
    @Value("${xpertsms.template.id}")
    private String templateId;

    // NEW: Entity ID
    @Value("${xpertsms.entity.id}")
    private String entityId;

    private final RestTemplate restTemplate;

    public SmsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends any DLT-registered message (body must match the registered template exactly).
     * Uses the same XpertSMS account as the OTP; only the template id differs.
     * Throws RuntimeException on failure so the caller can log it.
     */
    /** Sends one DLT SMS; returns the XpertSMS reply text. Throws when the gateway rejects it (HTTP error or error reply). */
    public String send(String mobileNumber, String body, String dltTemplateId) {
        java.net.URI uri = UriComponentsBuilder
                .fromUriString(smsApiUrl)
                .queryParam("key", smsApiKey)
                .queryParam("from", sender)
                .queryParam("to", mobileNumber)
                .queryParam("body", body)
                .queryParam("templateid", dltTemplateId)
                .queryParam("entityid", entityId)
                .encode()
                .build()
                .toUri();
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("XpertSMS failed: " + response.getStatusCode());
        }
        // XpertSMS answers HTTP 200 even when it rejects a message (template mismatch, bad number, no balance)
        String reply = response.getBody() == null ? "" : response.getBody().trim();
        if (reply.matches("(?is).*(error|fail|invalid|reject|insufficient|blocked|not allowed).*")) {
            throw new RuntimeException("XpertSMS rejected: " + reply);
        }
        return reply;
    }

    /** OTP calls only: give up quickly when XpertSMS does not answer, so the login can fall back to 123456. */
    private final RestTemplate otpRestTemplate = createOtpRestTemplate();

    private static RestTemplate createOtpRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory f =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5_000);
        f.setReadTimeout(10_000);
        return new RestTemplate(f);
    }

    /**
     * Sends the login OTP. Returns XpertSMS's message id when it accepted the message ("status":100 = submitted).
     * Throws on timeout, HTTP error or any other status, so the caller falls back to the default OTP.
     * Note: "submitted" is not "delivered" - delivery is only known from XpertSMS's delivery report.
     */
    public String sendOtp(String mobileNumber, String otp) {

        // Must match the registered DLT template exactly
        String smsBody =
                otp +
                        " is the One Time Password for verification of your login with RITES LTD- QA Division. " +
                        "Valid for 10 minutes. Please do not share with anyone. -RITES/QA";

        java.net.URI uri = UriComponentsBuilder
                .fromUriString(smsApiUrl)
                .queryParam("key", smsApiKey)
                .queryParam("from", sender)
                .queryParam("to", mobileNumber)
                .queryParam("body", smsBody)
                .queryParam("templateid", templateId)
                .queryParam("entityid", entityId)
                .build()
                .toUri();

        String reply;
        try {
            ResponseEntity<String> response = otpRestTemplate.getForEntity(uri, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("XpertSMS HTTP " + response.getStatusCode());
            }
            reply = response.getBody() == null ? "" : response.getBody().trim();
        } catch (Exception e) {
            System.out.println("XPERT SMS OTP ERROR: " + e.getMessage());
            throw new RuntimeException("Unable to send OTP", e);
        }

        // XpertSMS answers HTTP 200 even when it refuses a message; only "status":100 means it was accepted
        java.util.regex.Matcher status = java.util.regex.Pattern.compile("\"status\"\\s*:\\s*\"?(\\d+)").matcher(reply);
        if (!status.find() || !"100".equals(status.group(1))) {
            System.out.println("XPERT SMS OTP REJECTED: " + reply);
            throw new RuntimeException("XpertSMS did not accept the OTP: " + reply);
        }
        java.util.regex.Matcher id = java.util.regex.Pattern.compile("\"messageid\"\\s*:\\s*\"([^\"]*)\"").matcher(reply);
        String messageId = id.find() ? id.group(1) : "";
        System.out.println("XPERT SMS OTP SUBMITTED: messageid=" + messageId);
        return messageId;
    }
}
