package com.sarthi.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sarthi.constant.AppConstant;
import com.sarthi.dto.authenticator.AuthSessionResponseDto;
import com.sarthi.dto.authenticator.AuthStatusResponseDto;
import com.sarthi.dto.authenticator.InitiateAuthSessionRequestDto;
import com.sarthi.dto.authenticator.VerifyAuthenticatorRequestDto;
import com.sarthi.entity.mfa.AuthenticatorVerification;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.mfa.AuthenticatorVerificationRepository;
import com.sarthi.service.AuthenticatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticatorServiceImpl implements AuthenticatorService {

    private final AuthenticatorVerificationRepository verificationRepository;
    private final ObjectMapper objectMapper;

    private static final int SESSION_EXPIRY_SECONDS = 180; // 3 minutes

    @Override
    @Transactional
    public AuthSessionResponseDto initiateSession(InitiateAuthSessionRequestDto request, String ipAddress) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(SESSION_EXPIRY_SECONDS);

        AuthenticatorVerification verification = AuthenticatorVerification.builder()
                .sessionId(sessionId)
                .userId(request.getUserId())
                .userName(request.getUserName())
                .loginTransactionId(request.getLoginTransactionId())
                .status("PENDING")
                .createdAt(now)
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .build();

        verificationRepository.save(verification);

        // QR Code data payload formatted as JSON string
        Map<String, Object> qrPayloadMap = new HashMap<>();
        qrPayloadMap.put("app", "SARTHI_AUTHENTICATOR");
        qrPayloadMap.put("sessionId", sessionId);
        qrPayloadMap.put("userId", request.getUserId());
        qrPayloadMap.put("userName", request.getUserName() != null ? request.getUserName() : "");
        qrPayloadMap.put("timestamp", System.currentTimeMillis());

        String qrDataJson;
        try {
            qrDataJson = objectMapper.writeValueAsString(qrPayloadMap);
        } catch (Exception e) {
            qrDataJson = String.format("{\"app\":\"SARTHI_AUTHENTICATOR\",\"sessionId\":\"%s\",\"userId\":\"%s\"}", sessionId, request.getUserId());
        }

        log.info("Initiated authenticator session: {} for user: {}", sessionId, request.getUserId());

        return AuthSessionResponseDto.builder()
                .sessionId(sessionId)
                .qrData(qrDataJson)
                .status("PENDING")
                .expiresAt(expiresAt)
                .expiresInSeconds((long) SESSION_EXPIRY_SECONDS)
                .build();
    }

    @Override
    @Transactional
    public AuthStatusResponseDto getSessionStatus(String sessionId) {
        AuthenticatorVerification verification = verificationRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_RESOURCE, "Authentication session not found.")
                ));

        if ("PENDING".equals(verification.getStatus()) && LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            verification.setStatus("EXPIRED");
            verificationRepository.save(verification);
        }

        String msg;
        switch (verification.getStatus()) {
            case "AUTHENTICATED":
                msg = "Biometric & location verification successful.";
                break;
            case "SCANNED":
                msg = "QR code scanned. Waiting for location & selfie submission...";
                break;
            case "EXPIRED":
                msg = "Authentication session expired. Please refresh the QR code.";
                break;
            case "FAILED":
                msg = "Authentication failed.";
                break;
            default:
                msg = "Waiting for QR scan...";
        }

        return AuthStatusResponseDto.builder()
                .sessionId(verification.getSessionId())
                .status(verification.getStatus())
                .userId(verification.getUserId())
                .userName(verification.getUserName())
                .latitude(verification.getLatitude())
                .longitude(verification.getLongitude())
                .verifiedAt(verification.getVerifiedAt())
                .message(msg)
                .build();
    }

    @Override
    @Transactional
    public AuthStatusResponseDto verifyAuthenticator(VerifyAuthenticatorRequestDto request, String ipAddress) {
        AuthenticatorVerification verification = verificationRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(AppConstant.NO_RECORD_FOUND, AppConstant.ERROR_TYPE_CODE_RESOURCE, AppConstant.ERROR_TYPE_RESOURCE, "Authentication session not found.")
                ));

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            verification.setStatus("EXPIRED");
            verificationRepository.save(verification);
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_INVALID, AppConstant.ERROR_TYPE_CODE_INVALID, AppConstant.ERROR_TYPE_INVALID, "Session expired. Please scan a new QR code.")
            );
        }

        if ("AUTHENTICATED".equals(verification.getStatus())) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_INVALID, AppConstant.ERROR_TYPE_CODE_INVALID, AppConstant.ERROR_TYPE_INVALID, "This session has already been authenticated.")
            );
        }

        // Save location, selfie, and metadata
        verification.setLatitude(request.getLatitude());
        verification.setLongitude(request.getLongitude());
        verification.setAccuracyMeters(request.getAccuracyMeters());
        verification.setLocationAddress(request.getLocationAddress());
        verification.setSelfieImageUrl(request.getSelfieImageBase64());
        verification.setDeviceInfo(request.getDeviceInfo());
        if (ipAddress != null && !ipAddress.isEmpty()) {
            verification.setIpAddress(ipAddress);
        }
        verification.setStatus("AUTHENTICATED");
        verification.setVerifiedAt(LocalDateTime.now());

        verificationRepository.save(verification);

        log.info("Successfully verified authenticator session: {} for user: {} at lat: {}, long: {}",
                verification.getSessionId(), verification.getUserId(), request.getLatitude(), request.getLongitude());

        return AuthStatusResponseDto.builder()
                .sessionId(verification.getSessionId())
                .status("AUTHENTICATED")
                .userId(verification.getUserId())
                .userName(verification.getUserName())
                .latitude(verification.getLatitude())
                .longitude(verification.getLongitude())
                .verifiedAt(verification.getVerifiedAt())
                .message("Authentication verified successfully.")
                .build();
    }
}
