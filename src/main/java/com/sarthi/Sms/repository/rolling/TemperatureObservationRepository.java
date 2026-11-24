package com.sarthi.Sms.repository.rolling;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.rolling.TemperatureObservationEntity;
import com.sarthi.Sms.entity.rolling.TemperatureObservationId;
@Repository
public interface TemperatureObservationRepository extends JpaRepository<TemperatureObservationEntity, TemperatureObservationId>{
    List<TemperatureObservationEntity> findByIdDutyId(String dutyId);

    @Query(value = "SELECT t.created_at, t.finishing_temperature, t.soaking_zone_temperature " +
               "FROM rolling_temperature_observation t " +
               "JOIN rolling_duty d ON t.duty_id = d.duty_id " +
               "WHERE d.date = :date " +
               "AND d.shift = :shift " +
               "AND d.rail_grade = :railGrade " +
               "AND d.rail_section = :railSection And d.mill = :mill", 
       nativeQuery = true)
List<Object[]> findTemperatureObsDtls(@Param("date") LocalDate date,
                                       @Param("shift") String shift,
                                       @Param("railGrade") String railGrade,
                                       @Param("railSection") String railSection,
                                       @Param("mill") String mill);

}
