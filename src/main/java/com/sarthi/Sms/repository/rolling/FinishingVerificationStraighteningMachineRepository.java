package com.sarthi.Sms.repository.rolling;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sarthi.Sms.entity.rolling.FinishingVerificationStraignteningMachineEntity;
public interface FinishingVerificationStraighteningMachineRepository
        extends JpaRepository<FinishingVerificationStraignteningMachineEntity, String> {
    @Query(value = "SELECT sm.sm_r1_hrsm, sm.sm_r4_hrsm, sm.sm_r6_hrsm, sm.sm_r8_hrsm, " +
            "sm.sm_r1_vrsm, sm.sm_r4_vrsm, sm.sm_r6_vrsm, sm.sm_r8_vrsm, " +
            "sm.sm_is_satisfactory, sm.sm_sr_r1_hrsm, sm.sm_sr_r2_hrsm, sm.sm_sr_r3_hrsm, sm.sm_sr_r4_hrsm, " +
            "sm.sm_sr_r1_vrsm, sm.sm_sr_r2_vrsm, sm.sm_sr_r3_vrsm, sm.sm_sr_r4_vrsm, sm.sm_sr_is_satisfactory, " +
            "sm.sm_lr_r1_hrsm, sm.sm_lr_r2_hrsm, sm.sm_lr_r3_hrsm, sm.sm_lr_r4_hrsm, " +
            "sm.sm_lr_r1_vrsm, sm.sm_lr_r2_vrsm, sm.sm_lr_r3_vrsm, sm.sm_lr_r4_vrsm, sm.sm_lr_is_satisfactory, " +
            "sm.straigtness_remarks, sm.remarks " +
            "FROM rolling_finishing_verification_straightening_machine sm " +
            "JOIN rolling_duty rd ON rd.duty_id = sm.duty_id " +
            "WHERE rd.date = :date AND rd.shift = :shift AND rd.rail_section = :railSection AND rd.mill = :mill", nativeQuery = true)
    List<Object[]> getSmDtls(@Param("date") LocalDate date, @Param("shift") String shift,
            @Param("railSection") String railSection, @Param("mill") String mill);

    FinishingVerificationStraignteningMachineEntity findByDutyId(String dutyId);
}
