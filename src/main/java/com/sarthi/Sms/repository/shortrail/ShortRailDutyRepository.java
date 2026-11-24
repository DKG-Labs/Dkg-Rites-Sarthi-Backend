package com.sarthi.Sms.repository.shortrail;

import java.util.Optional;

import com.sarthi.Sms.entity.ndt.NdtDutyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.shortrail.ShortRailDutyEntity;
@Repository
public interface ShortRailDutyRepository extends JpaRepository<ShortRailDutyEntity, String> {

    @Query(value = "SELECT * FROM short_rail_duty WHERE user_id = :userId AND end_time IS NULL", nativeQuery = true)
    Optional<ShortRailDutyEntity> findOngoingDuty(@Param("userId") String userId);

    @Query(value = "SELECT duty_id FROM short_rail_duty WHERE user_id = :userId AND end_time IS NULL", nativeQuery = true)
    String checkDutyStatus(@Param("userId") String userId);

    Optional<ShortRailDutyEntity> findByDutyId(String dutyId);

    @Query(value="SELECT * FROM short_rail_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<ShortRailDutyEntity> getOngoingDutyDtls(@Param("userId") String userId);
}
