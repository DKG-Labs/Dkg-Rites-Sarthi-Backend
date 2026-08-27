package com.sarthi.service.Impl;

import com.sarthi.entity.UserMaster;
import com.sarthi.entity.mfa.LoginOtp;
import com.sarthi.repository.mfa.LoginOtpRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final LoginOtpRepository loginOtpRepository;

    private final SmsService smsService;

    // ============================================================
    // NO PasswordEncoder HERE
    // ============================================================

    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            LoginOtpRepository loginOtpRepository,
            SmsService smsService) {

        this.loginOtpRepository = loginOtpRepository;
        this.smsService = smsService;
    }

    public String generateAndSendOtp(UserMaster user) {

        // ============================================================
        // STEP 1: Generate 6 digit OTP
        // ============================================================

        String otp = String.valueOf(
                100000 + secureRandom.nextInt(900000)
        );

        // ============================================================
        // STEP 2: Create OTP entity
        // ============================================================

        LoginOtp loginOtp = new LoginOtp();

        loginOtp.setUserId(
                Long.valueOf(user.getUserId())
        );

        // Store OTP
        loginOtp.setOtp(otp);

        // OTP valid for 10 minutes
        loginOtp.setExpiresAt(
                LocalDateTime.now().plusMinutes(10)
        );

        // Maximum 5 attempts
        loginOtp.setAttemptCount(0);

        // OTP initially unused
        loginOtp.setUsed(false);

        // Creation time
        loginOtp.setCreatedAt(
                LocalDateTime.now()
        );

        // ============================================================
        // STEP 3: Save OTP
        // ============================================================

        LoginOtp savedOtp =
                loginOtpRepository.save(loginOtp);

        // ============================================================
        // STEP 4: Get registered mobile number & Send SMS
        // ============================================================

        String mobileNumber = user.getMobileNumber();

        System.out.println("=================================================");
        System.out.println("🔐 [SARTHI MFA OTP] User: " + (user.getEmployeeCode() != null ? user.getEmployeeCode() : user.getUsername()) + " | Role: " + user.getRoleName() + " | Mobile: " + (mobileNumber != null ? mobileNumber : "N/A") + " | Generated OTP: " + otp);
        System.out.println("=================================================");

        // Send live SMS if a valid mobile number is present for the user
        if (mobileNumber != null && !mobileNumber.isBlank()) {
            try {
                smsService.sendOtp(
                        mobileNumber.trim(),
                        otp
                );
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not deliver SMS to " + mobileNumber + ": " + e.getMessage());
            }
        }

        // ============================================================
        // STEP 5: Return transaction ID
        // ============================================================

        return String.valueOf(
                savedOtp.getId()
        );
    }
}