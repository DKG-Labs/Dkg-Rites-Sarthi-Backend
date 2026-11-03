package com.sarthi.repository.mfa;

import com.sarthi.entity.mfa.AuthenticatorVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthenticatorVerificationRepository extends JpaRepository<AuthenticatorVerification, Long> {
    Optional<AuthenticatorVerification> findBySessionId(String sessionId);
    Optional<AuthenticatorVerification> findTopByUserIdOrderByCreatedAtDesc(String userId);
    Optional<AuthenticatorVerification> findTopByUserIdAndStatusOrderByCreatedAtDesc(String userId, String status);
    boolean existsByUserIdAndStatusAndExpiresAtAfter(String userId, String status, LocalDateTime now);

    /** Row for the Active Login list. Leaves out the selfie (LONGTEXT) so the list stays fast. */
    interface ActiveLoginRow {
        Long getId();
        String getSessionId();
        String getUserId();
        String getUserName();
        String getLoginTransactionId();
        Double getLatitude();
        Double getLongitude();
        Double getAccuracyMeters();
        String getLocationAddress();
        Boolean getHasSelfie();
        String getDeviceInfo();
        String getIpAddress();
        String getStatus();
        LocalDateTime getCreatedAt();
        LocalDateTime getVerifiedAt();
        LocalDateTime getExpiresAt();
    }

    @Query(value = "select v.id as id, v.sessionId as sessionId, v.userId as userId, v.userName as userName, "
            + "v.loginTransactionId as loginTransactionId, v.latitude as latitude, v.longitude as longitude, "
            + "v.accuracyMeters as accuracyMeters, v.locationAddress as locationAddress, "
            + "(case when v.selfieImageUrl is null then false else true end) as hasSelfie, "
            + "v.deviceInfo as deviceInfo, v.ipAddress as ipAddress, v.status as status, "
            + "v.createdAt as createdAt, v.verifiedAt as verifiedAt, v.expiresAt as expiresAt "
            + "from AuthenticatorVerification v "
            + "where v.status = :status and v.expiresAt > :now "
            + "order by v.id desc")
    /**
     * Every currently valid login (authenticated, not expired, not logged out), newest first.
     * One simple filter, no sub-queries: the caller keeps the latest row per user and pages in memory.
     * This is a small set (people logged in right now), and the selfie column is left out.
     */
    java.util.List<ActiveLoginRow> findAllActive(@Param("status") String status,
                                                 @Param("now") LocalDateTime now);

    /** Ends every still-active session of a user (called on logout). */
    @Modifying
    @Transactional
    @Query("update AuthenticatorVerification v set v.status = :newStatus "
            + "where v.userId in :userIds and v.status = :status and v.expiresAt > :now")
    int endActiveSessions(@Param("userIds") java.util.Collection<String> userIds,
                          @Param("status") String status,
                          @Param("newStatus") String newStatus,
                          @Param("now") LocalDateTime now);
}
