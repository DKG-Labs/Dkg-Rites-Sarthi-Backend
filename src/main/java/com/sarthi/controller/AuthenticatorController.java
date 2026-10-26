package com.sarthi.controller;

import com.sarthi.dto.authenticator.AuthSessionResponseDto;
import com.sarthi.dto.authenticator.AuthStatusResponseDto;
import com.sarthi.dto.authenticator.InitiateAuthSessionRequestDto;
import com.sarthi.dto.authenticator.VerifyAuthenticatorRequestDto;
import com.sarthi.service.AuthenticatorService;
import com.sarthi.util.ResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/auth/authenticator")
@RequiredArgsConstructor
public class AuthenticatorController {

    private final AuthenticatorService authenticatorService;

    @PostMapping("/session/initiate")
    public ResponseEntity<Object> initiateSession(
            @RequestBody InitiateAuthSessionRequestDto request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        AuthSessionResponseDto response = authenticatorService.initiateSession(request, clientIp);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @GetMapping("/session/{sessionId}/status")
    public ResponseEntity<Object> getSessionStatus(@PathVariable String sessionId) {
        AuthStatusResponseDto response = authenticatorService.getSessionStatus(sessionId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity<Object> hasActiveLogin(@RequestParam String userId) {
        Map<String, Object> body = new HashMap<>();
        body.put("active", authenticatorService.hasActiveLogin(userId));
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(body), HttpStatus.OK);
    }

    @PostMapping("/verify")
    public ResponseEntity<Object> verifyAuthenticator(
            @RequestBody VerifyAuthenticatorRequestDto request,
            HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        AuthStatusResponseDto response = authenticatorService.verifyAuthenticator(request, clientIp);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
