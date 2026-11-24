package com.sarthi.Sms.repository.welding;

import com.sarthi.Sms.entity.welding.WeldingTestSampleEntity;
import com.sarthi.Sms.entity.welding.WeldingTestSampleKey;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeldingTestSampleRepository extends JpaRepository<WeldingTestSampleEntity, WeldingTestSampleKey> {

    @Query(value = """
                SELECT
                    wts.test_type AS testType,
                    wts.joint_number AS jointNo,
                    DATE_FORMAT(wid.date, '%d/%m/%Y') AS date,
                    wid.shift AS shift,
                    wts.plant AS machineNo
                FROM welding_test_sample wts
               LEFT JOIN welding_hardness wh
                 ON wts.test_type = wh.test_type AND wts.joint_number = wh.joint_number
                LEFT JOIN welding_micro wm
                    ON wts.test_type = wm.test_type AND wts.joint_number = wm.joint_number
                LEFT JOIN welding_macro wma
                    ON wts.test_type = wma.test_type AND wts.joint_number = wma.joint_number
                LEFT JOIN welding_tlt wt
                    ON wts.test_type = wt.test_type AND wts.joint_number = wt.joint_number
                JOIN welding_inspection_duty wid
                    ON wts.duty_id = wid.duty_id
                WHERE
                wh.test_type IS NULL
                    AND wm.test_type IS NULL
                    AND wma.test_type IS NULL
                    AND wt.test_type IS NULL
            """, nativeQuery = true)
    List<Object[]> findMissingRecords();

    @Query(value = """
                SELECT t.joint_number, COUNT(t.joint_number) AS row_count, d.date, d.shift, t.test_type
                FROM welding_tlt t
                JOIN welding_inspection_duty d ON t.duty_id = d.duty_id
                WHERE t.created_at = (
                    SELECT MAX(created_at)
                    FROM welding_tlt
                )
                GROUP BY t.joint_number, d.date, d.shift, t.test_type

                UNION ALL

                SELECT h.joint_number, COUNT(*) AS row_count, d.date, d.shift, h.test_type
                FROM welding_hardness h
                JOIN welding_inspection_duty d ON h.duty_id = d.duty_id
                WHERE h.created_at = (
                    SELECT MAX(created_at)
                    FROM welding_hardness
                )
                GROUP BY h.joint_number, d.date, d.shift, h.test_type

                UNION ALL

                SELECT m.joint_number, COUNT(m.joint_number) AS row_count, d.date, d.shift, m.test_type
                FROM welding_micro m
                JOIN welding_inspection_duty d ON m.duty_id = d.duty_id
                WHERE m.created_at = (
                    SELECT MAX(created_at)
                    FROM welding_micro
                )
                GROUP BY m.joint_number, d.date, d.shift, m.test_type

                UNION ALL

                SELECT ma.joint_number, COUNT(ma.joint_number) AS row_count, d.date, d.shift, ma.test_type
                FROM welding_macro ma
                JOIN welding_inspection_duty d ON ma.duty_id = d.duty_id
                WHERE ma.created_at = (
                    SELECT MAX(created_at)
                    FROM welding_macro
                )
                GROUP BY ma.joint_number, d.date, d.shift, ma.test_type
            """, nativeQuery = true)
    List<Object[]> getRecentTestSummary();

    @Query(value = """
                SELECT t.joint_number, d.date, d.shift, t.test_type, t.created_at
                FROM welding_tlt t
                JOIN welding_inspection_duty d ON t.duty_id = d.duty_id
                WHERE t.created_at = (SELECT MAX(created_at) FROM welding_tlt)

                UNION ALL

                SELECT h.joint_number, d.date, d.shift, h.test_type, h.created_at
                FROM welding_hardness h
                JOIN welding_inspection_duty d ON h.duty_id = d.duty_id
                WHERE h.created_at = (SELECT MAX(created_at) FROM welding_hardness)

                UNION ALL

                SELECT m.joint_number, d.date, d.shift, m.test_type, m.created_at
                FROM welding_micro m
                JOIN welding_inspection_duty d ON m.duty_id = d.duty_id
                WHERE m.created_at = (SELECT MAX(created_at) FROM welding_micro)

                UNION ALL

                SELECT ma.joint_number, d.date, d.shift, ma.test_type, ma.created_at
                FROM welding_macro ma
                JOIN welding_inspection_duty d ON ma.duty_id = d.duty_id
                WHERE ma.created_at = (SELECT MAX(created_at) FROM welding_macro)
            """, nativeQuery = true)
    List<Object[]> getRecentAddedRecord();

    @Query(value = """
                SELECT wtlt.test_type, COUNT(*) AS row_count FROM welding_tlt wtlt
                GROUP BY wtlt.test_type
                UNION ALL
                SELECT whdn.test_type, COUNT(*) AS row_count FROM welding_hardness whdn
                GROUP BY whdn.test_type
                UNION ALL
                SELECT wmic.test_type, COUNT(*) AS row_count FROM welding_micro wmic
                GROUP BY wmic.test_type
                UNION ALL
                SELECT wmac.test_type, COUNT(*) AS row_count FROM welding_macro wmac
                GROUP BY wmac.test_type
            """, nativeQuery = true)
    List<Object[]> getTestTypeJointCount();

    @Query(value = """
                SELECT
                    m.machine_number,
                    COALESCE(COUNT(DISTINCT t.machine_number), 0) AS tlt_count,
                    COALESCE(COUNT(DISTINCT h.machine_number), 0) AS hardness_count,
                    COALESCE(COUNT(DISTINCT ma.machine_number), 0) AS macro_count,
                    COALESCE(COUNT(DISTINCT mi.machine_number), 0) AS micro_count
                FROM (
                    SELECT 'BSP1' AS machine_number
                    UNION ALL
                    SELECT 'BSP2'
                    UNION ALL
                    SELECT 'BSP4'
                ) AS m
                LEFT JOIN welding_tlt t ON m.machine_number = t.machine_number
                LEFT JOIN welding_hardness h ON m.machine_number = h.machine_number
                LEFT JOIN welding_macro ma ON m.machine_number = ma.machine_number
                LEFT JOIN welding_micro mi ON m.machine_number = mi.machine_number
                GROUP BY m.machine_number
                ORDER BY m.machine_number
            """, nativeQuery = true)
    List<Object[]> getMachineWiseTestCount();

    @Query(value = """
            SELECT
                wid.mill AS mill,
                wid.rail_section AS railSection,
                wid.rail_grade AS railGrade,
                CONCAT(wid.mill, ' - ', wid.welding_line) AS inspectionWorkspot,
                wt.machine_number AS weldingMachineNumber,
                wts.joint_number AS tltJointNo,
                CONCAT(wid.date, ' ', wid.shift) AS tltMarkingDateShift,
                CASE WHEN wts.status THEN 'Pass' ELSE 'Fail' END AS tltTestingStatus,
                CONCAT(wt.load_applied, ' T / ', wt.deflection, ' mm') AS tltLoadDeflection,
                wt.remark AS tltRemark,
                wh.joint_number AS hardnessJointNo,
                wid.date AS hardnessMarkingDate,
                CASE WHEN wh.deviation_l <= 20 AND wh.deviation_r <= 20 THEN 'Pass' ELSE 'Fail' END AS hardnessTestingStatus,
                CONCAT('Left: ', wh.deviation_l, ', Right: ', wh.deviation_r) AS hardnessDeviation,
                wh.remark AS hardnessRemark,
                wm.joint_number AS macroMicroJointNo,
                wid.date AS macroMicroMarkingDate,
                CASE WHEN wm.head_l IS NOT NULL THEN 'Pass' ELSE 'Fail' END AS macroMicroTestingStatus,
                CONCAT('HAZ Full Rail Foot: ', wm.foot_l, ' mm') AS macroMicroHazFullRailFoot,
                CONCAT('HAZ One Side Foot: ', wm.foot_r, ' mm') AS macroMicroHazOneSideFoot,
                wm.remarks AS macroMicroRemark,
                widetail.rail_id_1_length AS railLength1,
                widetail.rail_id_2_length AS railLength2
            FROM
                welding_inspection_duty wid
            LEFT JOIN
                welding_duty_inspection_relation wdir ON wid.duty_id = wdir.duty_id
            LEFT JOIN
                welding_inspection_master wim ON wdir.inspection_master_id = wim.inspection_master_id
            LEFT JOIN
                welding_inspection_detail widetail ON wim.inspection_master_id = widetail.inspection_master_id
            LEFT JOIN
                welding_test_sample wts ON widetail.joint_number = wts.joint_number
            LEFT JOIN
                welding_tlt wt ON wts.test_type = 'TLT' AND wts.joint_number = wt.joint_number
            LEFT JOIN
                welding_hardness wh ON wts.test_type = 'HARDNESS' AND wts.joint_number = wh.joint_number
            LEFT JOIN
                welding_macro wm ON wts.test_type = 'MACRO' AND wts.joint_number = wm.joint_number
            WHERE
                wid.date BETWEEN :startDate AND :endDate
            ORDER BY
                wid.date, wid.mill, wid.rail_section, wid.rail_grade
            """, nativeQuery = true)
    List<Object[]> getTestSampleReport(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

}
