package com.sarthi.Sms.repository.shortrail;

import com.sarthi.Sms.entity.shortrail.ShortRailMasterEntity;
import com.sarthi.Sms.entity.shortrail.ShortRailMasterKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShortRailMasterRepository extends JpaRepository<ShortRailMasterEntity, ShortRailMasterKey> {

    // Finds a Short Rail Master record by duty ID
    // List<ShortRailMasterEntity> findByDutyId(String dutyId);

    @Query("SELECT s FROM ShortRailMasterEntity s WHERE s.id.dutyId = :dutyId")
    List<ShortRailMasterEntity> findByDutyId(@Param("dutyId") String dutyId);

    @Query(value = """
                SELECT srm.duty_id, srm.Rail_Section_Inspected, srm.Rail_Grade_Inspected,
                       srm.Acceptance_26m_CL_A, srm.Acceptance_26m_CL_A_0_1, srm.Acceptance_26m_CL_B, srm.Acceptance_26m_IU,
                       srm.Acceptance_25m_CL_A, srm.Acceptance_25m_CL_A_0_1, srm.Acceptance_25m_CL_B, srm.Acceptance_25m_IU,

                        srm.Acceptance_24m_CL_A,
                        srm.Acceptance_24m_CL_A_0_1,
                        srm.Acceptance_24m_CL_B,
                        srm.Acceptance_24m_IU,

                        srm.Acceptance_13m_CL_A,
                        srm.Acceptance_13m_CL_A_0_1,
                        srm.Acceptance_13m_CL_B,
                        srm.Acceptance_13m_IU,

                        srm.Acceptance_12m_CL_A,
                        srm.Acceptance_12m_CL_A_0_1,
                        srm.Acceptance_12m_CL_B,
                        srm.Acceptance_12m_IU,

                        srm.Acceptance_11m_CL_A,
                        srm.Acceptance_11m_CL_A_0_1,
                        srm.Acceptance_11m_CL_B,
                        srm.Acceptance_11m_IU,

                       srm.Acceptance_10m_CL_A, srm.Acceptance_10m_CL_A_0_1, srm.Acceptance_10m_CL_B, srm.Acceptance_10m_IU,

                       srm.rej_ref_rejection_26m, srm.rej_ref_cutbar_26m, srm.rej_ref_refinish_26m,
                       srm.rej_ref_rejection_13m, srm.rej_ref_cutbar_13m, srm.rej_ref_refinish_13m,
                       srm.utnp_rejection_26m, srm.utnp_cutbar_26m, srm.utnp_refinish_26m,
                       srm.utnp_rejection_13m, srm.utnp_cutbar_13m, srm.utnp_refinish_13m, srm.Remarks
                FROM short_rail_master srm
                JOIN short_rail_duty srd ON srm.duty_id = srd.duty_id
                WHERE srd.Date = :date AND srd.shift = :shift
                AND srm.Rail_Grade_Inspected = :railGrade AND srm.Rail_Section_Inspected = :railSection
            """, nativeQuery = true)
    List<Object[]> findShortRailMaster(
            @Param("date") LocalDate date,
            @Param("shift") String shift,
            @Param("railGrade") String railGrade,
            @Param("railSection") String railSection);

    @Query(value = """
                SELECT srra.defect_26m AS defect26m,
                       srra.defect_26m_count AS defect26mCount,
                       srra.defect_13m AS defect13m,
                       srra.defect_13m_count AS defect13mCount
                FROM short_rail_duty srd
                JOIN short_rail_rejection_analysis srra ON srd.duty_Id = srra.duty_id
                WHERE srd.Date = :date
                  AND srd.shift = :shift
                  AND srra.Rail_Grade_Inspected = :railGrade
                  AND srra.Rail_Section_Inspected = :railSection
            """, nativeQuery = true)
    List<Object[]> findSriRejectionAnalysisDtls(@Param("date") LocalDate date,
            @Param("shift") String shift,
            @Param("railGrade") String railGrade,
            @Param("railSection") String railSection);

            @Query(value = """
                WITH required_sms AS (
                    SELECT 'SMS 2' AS sms UNION ALL
                    SELECT 'SMS 3'
                ),
                required_rolling AS (
                    SELECT 'URM' AS mill UNION ALL
                    SELECT 'RSM'
                ),
                required_mills AS (
                    SELECT 'URM' AS mill, 'NDT 1' AS ndt UNION ALL
                    SELECT 'URM', 'NDT 2' UNION ALL
                    SELECT 'RSM', 'LR' UNION ALL
                    SELECT 'RSM', 'SR'
                ),
                required_visual AS (
                    SELECT 'URM' AS mill UNION ALL
                    SELECT 'RSM'
                ),
                required_welding AS (
                    SELECT 'URM' AS mill, 'L3' AS welding_line UNION ALL
                    SELECT 'URM', 'L4' UNION ALL
                    SELECT 'URM', 'L5' UNION ALL
                    SELECT 'URM', 'L6' UNION ALL
                    SELECT 'RSM', NULL
                )
            
                SELECT 
                    CONCAT('SMS Duty - ', rs.sms) AS shift_dtls,
                    sd.shift_remarks,
                    sd.start_time
                FROM required_sms rs
                LEFT JOIN sms_duty sd
                    ON rs.sms = sd.sms
                    AND sd.shift = :shift
                    AND sd.date = :date
            
                UNION ALL
            
                SELECT 
                    CONCAT('Rolling Stage - ', rr.mill) AS shift_dtls,
                    rd.shift_remarks,
                    rd.start_time
                FROM required_rolling rr
                LEFT JOIN rolling_duty rd
                    ON rr.mill = rd.mill
                    AND rd.shift = :shift
                    AND rd.date = :date
            
                UNION ALL
            
                SELECT 
                    CONCAT('NDT ', rm.mill, '-', rm.ndt) AS shift_dtls,
                    nd.shift_remarks,
                    nd.start_time
                FROM required_mills rm
                LEFT JOIN ndt_duty nd
                    ON rm.mill = nd.mill
                    AND rm.ndt = nd.ndt
                    AND nd.shift = :shift
                    AND nd.date = :date
            
                UNION ALL
            
                SELECT 
                    CONCAT('Visual Inspection - ', rv.mill) AS shift_dtls,
                    vid.shift_remarks,
                    vid.start_time
                FROM required_visual rv
                LEFT JOIN visual_inspection_duty vid
                    ON rv.mill = vid.mill
                    AND vid.shift = :shift
                    AND vid.date = :date
            
                UNION ALL
            
                SELECT 
                    CONCAT('Welding Stage - ', rw.mill,
                           CASE WHEN rw.welding_line IS NOT NULL THEN CONCAT('-', rw.welding_line) ELSE '' END
                    ) AS shift_dtls,
                    wd.shift_remarks,
                    wd.start_time
                FROM required_welding rw
                LEFT JOIN welding_inspection_duty wd
                    ON rw.mill = wd.mill
                    AND (rw.welding_line = wd.welding_line OR (rw.welding_line IS NULL AND wd.welding_line IS NULL))
                    AND wd.shift = :shift
                    AND wd.date = :date
            
                ORDER BY shift_dtls
                """, nativeQuery = true)
            List<Object[]> getWorkstationRemarks(@Param("shift") String shift, @Param("date") LocalDate date);
            
}