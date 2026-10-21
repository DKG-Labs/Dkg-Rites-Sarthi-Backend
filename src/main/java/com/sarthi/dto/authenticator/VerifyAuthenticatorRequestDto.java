package com.sarthi.dto.authenticator;

import lombok.Data;

@Data
public class VerifyAuthenticatorRequestDto {
    private String sessionId;
    private String userId;
    private Double latitude;
    private Double longitude;
    private Double accuracyMeters;
    private String locationAddress;
    private String selfieImageBase64;
    private String deviceInfo;
}
