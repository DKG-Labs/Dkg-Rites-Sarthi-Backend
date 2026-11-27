package com.sarthi.repository.mfa;

import com.sarthi.entity.mfa.LoginOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginOtpRepository
        extends JpaRepository<LoginOtp, Long> {

    Optional<LoginOtp> findTopByUserIdAndUsedFalseOrderByCreatedAtDesc(
            Long userId
    );

    /**
     * Latest normal-mode SMS OTPs (real code sent, 123456 not accepted) created in [since, before), newest first:
     * [used, used_default]. Used to spot SMS that stopped reaching users.
     */
    @org.springframework.data.jpa.repository.Query(value =
            "SELECT used, used_default FROM login_otp " +
            "WHERE otp <> '123456' AND default_allowed = 0 AND created_at >= :since AND created_at < :before " +
            "ORDER BY id DESC LIMIT 10", nativeQuery = true)
    java.util.List<Object[]> findLatestSmsOtpUse(@org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since,
                                                @org.springframework.data.repository.query.Param("before") java.time.LocalDateTime before);

}
