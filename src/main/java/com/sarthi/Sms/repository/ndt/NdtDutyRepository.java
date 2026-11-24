package com.sarthi.Sms.repository.ndt;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.ndt.NdtDutyEntity;
@Repository
public interface NdtDutyRepository extends JpaRepository<NdtDutyEntity, String> {

    @Query(value = "SELECT * FROM ndt_duty WHERE user_id = :userId AND end_time IS NULL", nativeQuery = true)
    Optional<NdtDutyEntity> findOngoingDuty(@Param("userId") String userId);

    @Query(value = "SELECT duty_id FROM ndt_duty WHERE user_id = :userId AND end_time IS NULL", nativeQuery = true)
    String checkDutyStatus(@Param("userId") String userId);

    Optional<NdtDutyEntity> findByDutyId(String dutyId);

    @Query(value="SELECT * FROM ndt_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<NdtDutyEntity> getOngoingDutyDtls(@Param("userId") String userId);
}
