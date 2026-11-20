package com.sarthi.service.Impl;

import com.sarthi.repository.mfa.AuthenticatorVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Most users close the tab or let the session time out instead of clicking Logout, so their
 * authenticator rows stayed AUTHENTICATED forever. Every 15 minutes, rows past expires_at are set to EXPIRED.
 * (Explicit logout still sets LOGGED_OUT.)
 */
@Component
@RequiredArgsConstructor
public class AuthenticatorSessionExpiryJob {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorSessionExpiryJob.class);

    private final AuthenticatorVerificationRepository verificationRepository;

    @Scheduled(fixedDelayString = "${authenticator.expiry-job.delay-ms:900000}", initialDelay = 60000)
    public void expireOldSessions() {
        try {
            int n = verificationRepository.expireOldSessions(LocalDateTime.now());
            if (n > 0) logger.info("[Authenticator] Marked {} expired session(s) as EXPIRED", n);
        } catch (Exception e) {
            logger.warn("[Authenticator] Session expiry job failed: {}", e.getMessage());
        }
    }
}
