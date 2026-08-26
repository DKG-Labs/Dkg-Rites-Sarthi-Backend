package com.sarthi.entity.mfa;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_otp")
@Data
public class LoginOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User for whom OTP was generated
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Store HASH of OTP, NOT the actual OTP
    @Column(name = "otp", nullable = false)
    private String otp;

    // OTP will expire after 10 minutes
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Number of wrong attempts
    @Column(name = "attempt_count")
    private Integer attemptCount = 0;

    // OTP can be used only once
    @Column(name = "used")
    private Boolean used = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}