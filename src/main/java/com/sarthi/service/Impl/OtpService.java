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

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class OtpResult {
        private String transactionId;
        private String noticeMessage;
    }

    public OtpResult generateAndSendOtp(UserMaster user) {
        String mobileNumber = user.getMobileNumber();
        boolean hasMobile = mobileNumber != null && !mobileNumber.trim().isEmpty();

        String otp = String.valueOf(
                100000 + secureRandom.nextInt(900000)
        );

        boolean smsDelivered = false;

        if (hasMobile) {
            try {
                smsService.sendOtp(
                        mobileNumber.trim(),
                        otp
                );
                smsDelivered = true;
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Could not deliver SMS to " + mobileNumber + ": " + e.getMessage());
                smsDelivered = false;
            }
        }

        String noticeMessage;
        String finalOtp;

        if (smsDelivered) {
            finalOtp = otp;
            String cleanMobile = mobileNumber.trim();
            String last4 = cleanMobile.length() >= 4 ? cleanMobile.substring(cleanMobile.length() - 4) : cleanMobile;
            noticeMessage = "OTP sent to your registered mobile number ending with •••• " + last4 + ".";
        } else {
            finalOtp = "123456";
            if (!hasMobile) {
                noticeMessage = "Mobile number not registered. Please enter default OTP 123456 to login.";
            } else {
                noticeMessage = "SMS service temporarily unavailable. Please enter default OTP 123456 to login.";
            }
        }

        LoginOtp loginOtp = new LoginOtp();
        loginOtp.setUserId(
                Long.valueOf(user.getUserId())
        );
        loginOtp.setOtp(finalOtp);
        loginOtp.setExpiresAt(
                LocalDateTime.now().plusMinutes(10)
        );
        loginOtp.setAttemptCount(0);
        loginOtp.setUsed(false);
        loginOtp.setCreatedAt(
                LocalDateTime.now()
        );

        LoginOtp savedOtp = loginOtpRepository.save(loginOtp);

        System.out.println("=================================================");
        System.out.println("🔐 [SARTHI MFA OTP] User: " + (user.getEmployeeCode() != null ? user.getEmployeeCode() : user.getUsername())
                + " | Role: " + user.getRoleName()
                + " | Mobile: " + (hasMobile ? mobileNumber : "N/A")
                + " | SMS Delivered: " + smsDelivered
                + " | Effective OTP: " + finalOtp);
        System.out.println("=================================================");

        return new OtpResult(
                String.valueOf(savedOtp.getId()),
                noticeMessage
        );
    }
}