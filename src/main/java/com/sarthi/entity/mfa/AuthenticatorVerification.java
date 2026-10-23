package com.sarthi.entity.mfa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "sarthi_authenticator_verifications", indexes = {
    @Index(name = "idx_auth_session", columnList = "session_id"),
    @Index(name = "idx_auth_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticatorVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 100)
    private String sessionId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "user_name", length = 150)
    private String userName;

    @Column(name = "login_transaction_id", length = 100)
    private String loginTransactionId;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "accuracy_meters")
    private Double accuracyMeters;

    @Column(name = "location_address", columnDefinition = "TEXT")
    private String locationAddress;

    @Lob
    @Column(name = "selfie_image_url", columnDefinition = "LONGTEXT")
    private String selfieImageUrl;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "status", length = 30)
    private String status; // PENDING, SCANNED, AUTHENTICATED, EXPIRED, FAILED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
