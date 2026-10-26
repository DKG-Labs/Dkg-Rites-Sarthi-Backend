package com.sarthi.repository.mfa;

import com.sarthi.entity.mfa.AuthenticatorVerification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthenticatorVerificationRepository extends JpaRepository<AuthenticatorVerification, Long> {
    Optional<AuthenticatorVerification> findBySessionId(String sessionId);
    Optional<AuthenticatorVerification> findTopByUserIdOrderByCreatedAtDesc(String userId);
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
            + "and (:search = '' or lower(v.userName) like lower(concat('%', :search, '%'))) "
            + "order by v.verifiedAt desc, v.id desc",
            countQuery = "select count(v) from AuthenticatorVerification v "
            + "where v.status = :status and v.expiresAt > :now "
            + "and (:search = '' or lower(v.userName) like lower(concat('%', :search, '%')))")
    Page<ActiveLoginRow> findActiveLogins(@Param("status") String status,
                                          @Param("now") LocalDateTime now,
                                          @Param("search") String search,
                                          Pageable pageable);
}
