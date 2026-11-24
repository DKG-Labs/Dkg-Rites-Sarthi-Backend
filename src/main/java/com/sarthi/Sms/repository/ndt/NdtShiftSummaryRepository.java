package com.sarthi.Sms.repository.ndt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.ndt.NdtShiftSummaryEntity;
@Repository
public interface NdtShiftSummaryRepository extends JpaRepository<NdtShiftSummaryEntity, String> {

    // Custom query method to find shift summary by dutyId
    Optional<NdtShiftSummaryEntity> findByDutyId(String dutyId);

    @Query(value = """
        SELECT
            ndt_d.date,
            ndt_d.ndt,
            ndt_d.rail_grade,
            ndt_d.rail_section,
            ndt_s.total_rails_inspected,
            ndt_s.total_ut_marked_rails,
            ndt_s.total_ut_suspected_rails,
            ndt_s.total_recycled_rails,
            ndt_s.total_ut_rejected_rails,
            ndt_s.ect_suspected_rails,
            ndt_s.body_straightness_result_a,
            ndt_s.body_straightness_result_a_1,
            ndt_s.body_straightness_result_marked,
            ndt_s.total_profile_marked,
            ndt_s.rail_length
        FROM
            ndt_duty ndt_d
        JOIN
            ndt_shift_summary ndt_s ON ndt_d.duty_id = ndt_s.duty_id
        WHERE
            ndt_d.date BETWEEN :startDate AND :endDate
        ORDER BY
            ndt_d.duty_id
    """, nativeQuery = true)
    List<Object[]> getNdtReport(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

}

