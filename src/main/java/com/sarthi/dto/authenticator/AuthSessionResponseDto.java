package com.sarthi.dto.authenticator;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthSessionResponseDto {
    private String sessionId;
    private String qrData;
    private String status;
    private LocalDateTime expiresAt;
    private Long expiresInSeconds;
}
