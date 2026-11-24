package com.sarthi.Sms.repository.rolling;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.rolling.RollingDutyEntity;
@Repository
public interface RollingDutyRepository extends JpaRepository<RollingDutyEntity, String> {
  @Query(value = "SELECT * FROM rolling_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
  Optional<RollingDutyEntity> findOngoingDuty(@Param("userId") String userId);

  @Query(value = "SELECT duty_id FROM rolling_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
  String checkDutyStatus(@Param("userId") String userId);

  Optional<RollingDutyEntity> findByDutyId(String dutyId);

  @Query(value = "SELECT * FROM rolling_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
  Optional<RollingDutyEntity> getOngoingDutyDtls(@Param("userId") String userId);

  @Query(value = """
          SELECT duty_id, vernier_number, micrometer_number, number_of_gauges
          FROM rolling_duty
          WHERE date = :date
            AND shift = :shift
            AND rail_grade = :railGrade
            AND rail_section = :railSection
            AND (:mill IS NULL OR mill = :mill)
      """, nativeQuery = true)
  List<Object[]> findAllRollingDuties(
      @Param("date") LocalDate date,
      @Param("shift") String shift,
      @Param("railGrade") String railGrade,
      @Param("railSection") String railSection,
      @Param("mill") String mill);

  @Query(value = """
          SELECT heat_number, sample_number, timing, sample_location, height, head,
                 flange, web, asy, foot_toe, fishing_height, weight, crown_profile,
                 foot_flatness, remark
          FROM rolling_control_heat_detail
          WHERE duty_id = :dutyId
      """, nativeQuery = true)
  List<Object[]> findHeatDetailsByDutyId(@Param("dutyId") String dutyId);
}
