package com.sarthi.Sms.repository.qct;

import com.sarthi.Sms.entity.ndt.NdtDutyEntity;
import com.sarthi.Sms.entity.qct.QctDutyEntity;
import com.sarthi.Sms.entity.rolling.RollingDutyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QctDutyRepository extends JpaRepository<QctDutyEntity, String> {

    @Query(value = "SELECT duty_id FROM qct_duty WHERE user_id = :userId AND end_time IS NULL", nativeQuery = true)
    String checkDutyStatus(@Param("userId") String userId);

    Optional<QctDutyEntity> findByDutyId(String dutyId);

    @Query(value="SELECT * FROM qct_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<QctDutyEntity> getOngoingDutyDtls(@Param("userId") String userId);
}
