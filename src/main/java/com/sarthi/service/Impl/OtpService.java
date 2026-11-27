package com.sarthi.service.Impl;

import com.sarthi.entity.UserMaster;
import com.sarthi.entity.mfa.LoginOtp;
import com.sarthi.repository.mfa.LoginOtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Login OTP by SMS, with the default OTP 123456 so users are never locked out when SMS does not reach them.
 *
 *  Only 123456 (no SMS):   the user has no mobile number, or XpertSMS fails / times out / does not accept the message.
 *  SMS code OR 123456:     a real SMS OTP is sent and 123456 is also accepted for this login when
 *                            (A) SMS looks down: of the last 10 normal SMS OTPs (older than 2 min) fewer than 2 were
 *                                used with the SMS code -> fallback mode for 30 min,
 *                            (B) this user's previous SMS OTP (last 10 min) was never used - it did not arrive,
 *                            (M) manual switch sms.otp.force-default=true.
 *  Only the SMS code:      normal.
 *  (C) In fallback mode the first login made with the real SMS code proves SMS works again and ends fallback at once.
 */
@Service
public class OtpService {

    static final String DEFAULT_OTP = "123456";

    private static final int SAMPLE_SIZE = 10;          // last N normal SMS OTPs looked at
    private static final int MIN_USED_IN_SAMPLE = 2;    // fewer real-code logins than this => SMS down
    private static final int GRACE_MINUTES = 2;         // newest OTPs are skipped (users still typing)
    private static final int LOOKBACK_MINUTES = 30;     // only recent OTPs count
    private static final int FALLBACK_MINUTES = 30;     // fallback length (ends earlier on a real-code login)
    private static final int PREVIOUS_OTP_MINUTES = 10; // rule B: previous unused SMS OTP this recent
    private static final long CHECK_EVERY_MS = 60_000;  // re-check at most once a minute

    private final LoginOtpRepository loginOtpRepository;
    private final SmsService smsService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${sms.otp.force-default:false}")
    private boolean forceDefaultOtp;

    /** Fallback mode until this time (null = normal) */
    private volatile LocalDateTime fallbackUntil = null;
    /** Normal mode (re)started here; OTPs sent before it do not count for rule A */
    private volatile LocalDateTime normalSince = null;
    private volatile long lastCheckMs = 0;

    public OtpService(LoginOtpRepository loginOtpRepository, SmsService smsService) {
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
        Long userId = Long.valueOf(user.getUserId());

        String otp = String.valueOf(100000 + secureRandom.nextInt(900000));

        boolean smsSubmitted = false;
        boolean defaultAllowed = false;
        String messageId = null;
        String reason = null;

        if (!hasMobile) {
            reason = "no mobile";
        } else {
            // decided before the new OTP row exists, so rule B sees the previous one
            String allowReason = forceDefaultOtp ? "manual switch"
                    : smsLooksDown() ? "SMS fallback mode"
                    : previousSmsOtpUnused(userId) ? "previous SMS OTP not used"
                    : null;
            try {
                messageId = smsService.sendOtp(mobileNumber.trim(), otp);
                smsSubmitted = true;
                defaultAllowed = allowReason != null;
                reason = allowReason;
            } catch (Exception e) {
                reason = "send failed: " + e.getMessage();
            }
        }

        String finalOtp = smsSubmitted ? otp : DEFAULT_OTP;
        String noticeMessage;
        if (smsSubmitted) {
            String m = mobileNumber.trim();
            String last4 = m.length() >= 4 ? m.substring(m.length() - 4) : m;
            noticeMessage = "OTP sent to your registered mobile number ending with •••• " + last4 + "."
                    + (defaultAllowed ? " If it does not arrive, enter default OTP 123456." : "");
        } else {
            noticeMessage = hasMobile
                    ? "SMS service temporarily unavailable. Please enter default OTP 123456 to login."
                    : "Mobile number not registered. Please enter default OTP 123456 to login.";
        }

        LoginOtp loginOtp = new LoginOtp();
        loginOtp.setUserId(userId);
        loginOtp.setOtp(finalOtp);
        loginOtp.setDefaultAllowed(defaultAllowed);
        loginOtp.setUsedDefault(false);
        loginOtp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        loginOtp.setAttemptCount(0);
        loginOtp.setUsed(false);
        loginOtp.setCreatedAt(LocalDateTime.now());

        LoginOtp savedOtp = loginOtpRepository.save(loginOtp);

        // Never log the OTP or the full mobile number
        System.out.println("[SARTHI MFA OTP] User: "
                + (user.getEmployeeCode() != null ? user.getEmployeeCode() : user.getUsername())
                + " | Mobile: " + maskMobile(mobileNumber)
                + (smsSubmitted
                    ? " | SMS submitted to XpertSMS (messageid " + messageId + ")"
                        + (defaultAllowed ? " | 123456 also accepted (" + reason + ")" : "")
                    : " | Default OTP only (" + reason + ")"));

        return new OtpResult(String.valueOf(savedOtp.getId()), noticeMessage);
    }

    /**
     * Called after a login with the real SMS code on an OTP that also accepted 123456:
     * the SMS reached the user, so SMS works - leave fallback mode now (rule C).
     */
    public void smsCodeWorked() {
        if (fallbackUntil != null) {
            fallbackUntil = null;
            normalSince = LocalDateTime.now();
            System.out.println("[SARTHI MFA OTP] A user logged in with the SMS code - SMS works again, fallback ended");
        }
    }

    /** Rule A (+ fallback timer). */
    private boolean smsLooksDown() {
        LocalDateTime now = LocalDateTime.now();
        if (fallbackUntil != null) {
            if (now.isBefore(fallbackUntil)) return true;
            fallbackUntil = null;              // time is up: back to normal, judge only new sends
            normalSince = now;
            return false;
        }

        long nowMs = System.currentTimeMillis();
        if (nowMs - lastCheckMs < CHECK_EVERY_MS) return false;
        lastCheckMs = nowMs;

        try {
            LocalDateTime since = now.minusMinutes(LOOKBACK_MINUTES);
            if (normalSince != null && normalSince.isAfter(since)) since = normalSince;
            List<Object[]> rows = loginOtpRepository.findLatestSmsOtpUse(since, now.minusMinutes(GRACE_MINUTES));
            if (rows == null || rows.size() < SAMPLE_SIZE) return false;   // not enough to judge
            long usedWithSmsCode = rows.stream()
                    .filter(r -> isTrue(r[0]) && !isTrue(r[1]))
                    .count();
            if (usedWithSmsCode < MIN_USED_IN_SAMPLE) {
                fallbackUntil = now.plusMinutes(FALLBACK_MINUTES);
                System.out.println("[SARTHI MFA OTP] SMS OTPs not reaching users (" + usedWithSmsCode + " of last "
                        + rows.size() + " used) - 123456 also accepted until " + fallbackUntil
                        + " or until someone logs in with the SMS code");
                return true;
            }
        } catch (Exception e) {
            System.out.println("[SARTHI MFA OTP] SMS health check failed: " + e.getMessage());
        }
        return false;
    }

    /** Rule B: the user's latest earlier OTP was a real SMS code, recent, and never used (it did not arrive). */
    private boolean previousSmsOtpUnused(Long userId) {
        try {
            Optional<LoginOtp> prev = loginOtpRepository.findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(userId);
            return prev.isPresent()
                    && !DEFAULT_OTP.equals(prev.get().getOtp())
                    && prev.get().getCreatedAt() != null
                    && prev.get().getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(PREVIOUS_OTP_MINUTES));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isTrue(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        return "1".equals(v.toString()) || "true".equalsIgnoreCase(v.toString());
    }

    private static String maskMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) return "N/A";
        String m = mobile.trim();
        return m.length() <= 4 ? "****" : "******" + m.substring(m.length() - 4);
    }
}
