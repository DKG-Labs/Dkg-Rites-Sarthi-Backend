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
}
