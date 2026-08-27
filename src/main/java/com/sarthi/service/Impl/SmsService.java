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

    public void sendOtp(String mobileNumber, String otp) {

        /*
         * OTP SMS body
         *
         * IMPORTANT:
         * The template should match the registered SMS template.
         */

        String smsBody =
                otp +
                        " is the One Time Password for verification of your login with RITES LTD- QA Division. " +
                        "Valid for 10 minutes. Please do not share with anyone. -RITES/QA";

        /*
         * Build XpertSMS URL
         */
        UriComponentsBuilder builder =
                UriComponentsBuilder
                        .fromUriString(smsApiUrl)
                        .queryParam("key", smsApiKey)
                        .queryParam("from", sender)
                        .queryParam("to", mobileNumber)
                        .queryParam("body", smsBody)

                        // NEW
                        .queryParam("templateid", templateId)

                        // NEW
                        .queryParam("entityid", entityId);

        try {
            java.net.URI uri = builder.build().toUri();
            ResponseEntity<String> response =
                    restTemplate.getForEntity(
                            uri,
                            String.class
                    );

            /*
             * Temporary debugging
             *
             * Don't print API key or OTP.
             */

            System.out.println(
                    "XPERT SMS STATUS: "
                            + response.getStatusCode()
            );

            System.out.println(
                    "XPERT SMS RESPONSE: "
                            + response.getBody()
            );

            if (!response.getStatusCode().is2xxSuccessful()) {

                throw new RuntimeException(
                        "XpertSMS failed: "
                                + response.getStatusCode()
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "XPERT SMS ERROR: "
                            + e.getMessage()
            );

            throw new RuntimeException(
                    "Unable to send OTP",
                    e
            );
        }
    }
}