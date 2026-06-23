package com.sarthi.Sms.repository.sms;



import com.sarthi.Sms.entity.sms.SmsDutyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsDutyRepository extends JpaRepository<SmsDutyEntity, String> {
    @Query(value="SELECT * FROM sms_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<SmsDutyEntity> findOngoingDuty(@Param("userId") String userId);

    @Query(value="SELECT duty_id FROM sms_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    String checkDutyStatus(@Param("userId") String userId);

    Optional<SmsDutyEntity> findByDutyId(String dutyId);

    @Query(value="SELECT * FROM sms_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<SmsDutyEntity> getOngoingDutyDtls(@Param("userId") String userId);
}
