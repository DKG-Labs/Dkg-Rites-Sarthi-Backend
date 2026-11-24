package com.sarthi.Sms.repository.welding;

import com.sarthi.Sms.entity.visualInspection.ViDutyEntity;
import com.sarthi.Sms.entity.welding.WeldingInspectionDutyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeldingInspectionDutyRepository extends JpaRepository<WeldingInspectionDutyEntity, String> {

    @Query(value = "SELECT * FROM welding_inspection_duty WHERE user_id = :userId AND end_time IS NULL", nativeQuery = true)
    Optional<WeldingInspectionDutyEntity> findOngoingDuty(@Param("userId") String userId);

    @Query(value = "SELECT duty_id FROM welding_inspection_duty WHERE user_id = :userId AND end_time IS NULL", nativeQuery = true)
    String checkDutyStatus(@Param("userId") String userId);

    Optional<WeldingInspectionDutyEntity> findByDutyId(String dutyId);

     @Query(value="SELECT * FROM welding_inspection_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<WeldingInspectionDutyEntity> getOngoingDutyDtls(@Param("userId") String userId);
}
