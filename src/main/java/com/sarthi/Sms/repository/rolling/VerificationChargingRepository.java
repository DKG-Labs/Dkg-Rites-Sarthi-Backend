package com.sarthi.Sms.repository.rolling;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.rolling.VerificationChargingEntity;
@Repository
public interface VerificationChargingRepository extends JpaRepository<VerificationChargingEntity, String>{
    @Query(value = "SELECT v.heat_number, v.length, v.defect, v.bloom_tracking_verification, v.hot_blooms_descaling " +
                   "FROM rolling_verification_charging_detail v " +
                   "JOIN rolling_duty d ON v.duty_id = d.duty_id " +
                   "WHERE d.date = :date AND d.shift = :shift " +
                   "AND d.rail_grade = :railGrade AND d.rail_section = :railSection AND d.mill = :mill", 
           nativeQuery = true)
    List<Object[]> getChargingVerificationDtls(@Param("date") LocalDate date,
                                                @Param("shift") String shift,
                                                @Param("railGrade") String railGrade,
                                                @Param("railSection") String railSection,
                                                @Param("mill") String mill);
}



