package com.sarthi.repository.mfa;

import com.sarthi.entity.mfa.AuthenticatorVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthenticatorVerificationRepository extends JpaRepository<AuthenticatorVerification, Long> {
    Optional<AuthenticatorVerification> findBySessionId(String sessionId);
    Optional<AuthenticatorVerification> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
