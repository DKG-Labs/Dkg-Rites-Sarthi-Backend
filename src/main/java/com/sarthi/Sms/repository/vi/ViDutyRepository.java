package com.sarthi.Sms.repository.vi;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.visualInspection.ViDutyEntity;
@Repository
public interface ViDutyRepository extends JpaRepository<ViDutyEntity, String>{
    @Query(value="SELECT * FROM visual_inspection_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<ViDutyEntity> findOngoingDuty(@Param("userId") String userId);

    @Query(value="SELECT duty_id FROM visual_inspection_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    String checkDutyStatus(@Param("userId") String userId);

    Optional<ViDutyEntity> findByDutyId(String dutyId);

     @Query(value="SELECT * FROM visual_inspection_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<ViDutyEntity> getOngoingDutyDtls(@Param("userId") String userId);
}
