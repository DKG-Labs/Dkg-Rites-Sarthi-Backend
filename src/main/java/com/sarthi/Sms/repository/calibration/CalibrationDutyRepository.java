package com.sarthi.Sms.repository.calibration;

import com.sarthi.Sms.entity.calibration.CalibrationDutyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalibrationDutyRepository extends JpaRepository<CalibrationDutyEntity, String> {

    @Query(value = "SELECT * FROM calibration_duty WHERE user_id = :userId AND end_time IS NULL", nativeQuery = true)
    Optional<CalibrationDutyEntity> findOngoingDuty(@Param("userId") String userId);

    @Query(value = "SELECT duty_id FROM calibration_duty WHERE user_id = :userId AND end_time IS NULL", nativeQuery = true)
    String checkDutyStatus(@Param("userId") String userId);

    Optional<CalibrationDutyEntity> findByDutyId(String dutyId);

    @Query(value="SELECT * FROM calibration_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<CalibrationDutyEntity> getOngoingDutyDtls(@Param("userId") String userId);
}
