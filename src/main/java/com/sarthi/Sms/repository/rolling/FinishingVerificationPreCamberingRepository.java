package com.sarthi.Sms.repository.rolling;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sarthi.Sms.entity.rolling.FinishingVerificationPreCamberingEntity;
public interface FinishingVerificationPreCamberingRepository extends JpaRepository<FinishingVerificationPreCamberingEntity, String> {
    @Query(value = "SELECT fvp.heat_number, fvp.is_satisfactory " +
                   "FROM rolling_finishing_verification_pre_cambering fvp " +
                   "JOIN rolling_duty rd ON rd.duty_id = fvp.duty_id " +
                   "WHERE rd.date = :date AND rd.shift = :shift AND rd.rail_section = :railSection AND rd.mill = :mill", nativeQuery = true)
    List<Object[]> getPcDtls(@Param("date") LocalDate date, @Param("shift") String shift, @Param("railSection") String railSection, @Param("mill") String mill);

    List<FinishingVerificationPreCamberingEntity> findByDutyId(String dutyId);
}
