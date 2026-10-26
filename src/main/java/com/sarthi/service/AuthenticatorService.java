package com.sarthi.service;

import com.sarthi.dto.authenticator.AuthSessionResponseDto;
import com.sarthi.dto.authenticator.AuthStatusResponseDto;
import com.sarthi.dto.authenticator.InitiateAuthSessionRequestDto;
import com.sarthi.dto.authenticator.VerifyAuthenticatorRequestDto;

public interface AuthenticatorService {
    AuthSessionResponseDto initiateSession(InitiateAuthSessionRequestDto request, String ipAddress);
    AuthStatusResponseDto getSessionStatus(String sessionId);
    AuthStatusResponseDto verifyAuthenticator(VerifyAuthenticatorRequestDto request, String ipAddress);
    boolean hasActiveLogin(String userId);
}
