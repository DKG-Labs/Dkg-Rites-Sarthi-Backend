package com.sarthi.dto.authenticator;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthStatusResponseDto {
    private String sessionId;
    private String status; // PENDING, SCANNED, AUTHENTICATED, EXPIRED, FAILED
    private String userId;
    private String userName;
    private Double latitude;
    private Double longitude;
    private LocalDateTime verifiedAt;
    private String message;
}
