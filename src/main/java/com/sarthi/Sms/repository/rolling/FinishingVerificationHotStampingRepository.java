package com.sarthi.Sms.repository.rolling;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.rolling.FinishingVerificationHotStampingEntity;
@Repository
public interface FinishingVerificationHotStampingRepository extends JpaRepository<FinishingVerificationHotStampingEntity, String> {
     @Query(value = "SELECT hfvhs.heat_number, hfvhs.brand " +
                   "FROM rolling_finishing_verification_hot_stamping hfvhs " +
                   "JOIN rolling_duty rd ON rd.duty_id = hfvhs.duty_id " +
                   "WHERE rd.date = :date AND rd.shift = :shift AND rd.rail_section = :railSection AND rd.mill = :mill", nativeQuery = true)
    List<Object[]> getHsDtls(@Param("date") LocalDate date, @Param("shift") String shift, @Param("railSection") String railSection, @Param("mill") String mill);

    List<FinishingVerificationHotStampingEntity> findByDutyId(String dutyId);
}
