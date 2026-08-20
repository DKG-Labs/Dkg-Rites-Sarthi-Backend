package com.sarthi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "SMTP Test Controller", description = "Endpoints for testing SMTP email connectivity")
public class TestEmailController {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:sarthi.qa@rites.com}")
    private String senderMail;

    @Operation(summary = "Send a test email to verify SMTP connection and authentication")
    @GetMapping("/send-email")
    public ResponseEntity<Map<String, Object>> testSendEmail(
            @RequestParam(defaultValue = "jayakishore7077@gmail.com") String to) {

        Map<String, Object> response = new HashMap<>();
        try {
            log.info("Attempting to send test email to {}", to);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderMail);
            helper.setTo(to);
            helper.setSubject("SARTHI - SMTP Test Email");
            helper.setText("<h3>SARTHI SMTP Integration Test</h3><p>This is a test email sent from SARTHI backend via <b>smtp.mgovcloud.in:465</b>.</p><p>Status: <b>SMTP connection and authentication successful!</b></p>", true);

            mailSender.send(message);

            log.info("Test email sent successfully to {}", to);
            response.put("status", "SUCCESS");
            response.put("message", "Test email sent successfully to " + to);
            response.put("from", senderMail);
            response.put("host", "smtp.mgovcloud.in:465");
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("Failed to send test email to {}: {}", to, ex.getMessage(), ex);
            response.put("status", "FAILED");
            response.put("error", ex.getClass().getSimpleName());
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
