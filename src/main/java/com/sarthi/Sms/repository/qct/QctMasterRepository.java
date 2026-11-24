package com.sarthi.Sms.repository.qct;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.qct.QctMasterEntity;
// public interface QctMasterRepository extends JpaRepository<QctMasterEntity, QctMasterId> {
//     Optional<QctMasterEntity> findByIdDutyId(String dutyId);
//     List<QctMasterEntity> findAll();
// }


@Repository
public interface QctMasterRepository extends JpaRepository<QctMasterEntity, Integer> {
    @Query(value = """
        SELECT 
            qm.qct_Type AS qctType,
            qm.mill AS mill,
            qm.rail_grade AS railGrade,
            qm.rail_section AS railSection,
            DATE_FORMAT(qm.created_at, '%Y-%m') AS sampleMonth,
            COUNT(qm.qct_id) AS sampleCount
        FROM qct_master qm
        LEFT JOIN qct_duty qd ON qm.duty_Id = qd.duty_Id
        LEFT JOIN qct_fatigue_detail qfm ON qm.qct_id = qfm.qct_id
        LEFT JOIN qct_residual_master qrm ON qm.qct_id = qrm.qct_id
        LEFT JOIN qct_fcgr_master qfcgr ON qm.qct_id = qfcgr.qct_id
        LEFT JOIN qct_fracture_toughness_master qftm ON qm.qct_id = qftm.qct_id
        LEFT JOIN qct_center_line_master qclm ON qm.qct_id = qclm.qct_id
        WHERE 
            qfm.qct_id IS NULL 
            AND qrm.qct_id IS NULL 
            AND qfcgr.qct_id IS NULL 
            AND qftm.qct_id IS NULL 
            AND qclm.qct_id IS NULL
        GROUP BY 
            qm.qct_Type, qm.mill, qm.rail_grade, qm.rail_section, sampleMonth
    """, nativeQuery = true)
    List<Object[]> findPendingTests();

    // @Query(value = """
    //         SELECT 
    //             qm.heat_no,
    //             qm.strand_no,
    //             qm.sample_id,
    //             qm.mill,
    //             qm.rail_grade AS railGrade,
    //             qm.rail_section AS railSection,
    //             DATE_FORMAT(qm.created_at, '%m-%Y') AS sampleMonth,
    //             qm.qct_type,
    //             qm.qct_id
    //         FROM 
    //             qct_master qm
    //         JOIN 
    //             qct_duty qd ON qm.duty_id = qd.duty_id
    //         WHERE 
    //             qd.duty_id = :dutyId
    //         """, nativeQuery = true)
    // List<Object[]> findQctDtlByDutyId(@Param("dutyId") String dutyId);

    @Query(value = """
        SELECT 
            qm.heat_no,
            qm.strand_no,
            qm.sample_id,
            qm.mill,
            qm.rail_grade AS railGrade,
            qm.rail_section AS railSection,
            DATE_FORMAT(qm.created_at, '%m-%Y') AS sampleMonth,
            qm.qct_type,
            qm.qct_id
        FROM 
            qct_master qm
        LEFT JOIN 
            qct_fatigue_detail qfm ON qm.qct_id = qfm.qct_id
        LEFT JOIN 
            qct_residual_master qrm ON qm.qct_id = qrm.qct_id
        LEFT JOIN 
            qct_fcgr_master qfcgr ON qm.qct_id = qfcgr.qct_id
        LEFT JOIN 
            qct_fracture_toughness_master qftm ON qm.qct_id = qftm.qct_id
        LEFT JOIN 
            qct_center_line_master qclm ON qm.qct_id = qclm.qct_id
        JOIN 
            qct_duty qd ON qm.duty_id = qd.duty_id
        WHERE 
            qfm.qct_id IS NULL 
            AND qrm.qct_id IS NULL 
            AND qfcgr.qct_id IS NULL 
            AND qftm.qct_id IS NULL 
            AND qclm.qct_id IS NULL
            AND qd.duty_id = :dutyId
    """, nativeQuery = true)
List<Object[]> findQctDtlByDutyId(@Param("dutyId") String dutyId);


    @Query(value = """
            SELECT 
                qm.heat_no,
                qm.strand_no,
                qm.sample_id,
                qm.mill,
                qm.rail_grade,
                qm.rail_section,
                DATE_FORMAT(qm.created_at, '%m-%Y') AS sampleMonth,  -- Formatting the date to MM-YYYY
                qm.qct_type

            FROM 
                qct_master qm
            WHERE 
                qm.qct_id = :qctId
            """, nativeQuery = true)
    List<Object[]> findQctDtlByQctId(@Param("qctId") Integer qctId);

    @Query(nativeQuery = true, value = """
        SELECT
            qm.mill,
            qm.rail_grade,
            qm.rail_section,
            qm.heat_no,
            qm.strand_no,
            qm.sample_id,
            CONCAT(
                'Strain: ', qfm.strain, ', ',
                'Frequency: ', qfm.frequency, ', ',
                'Stress: ', qfm.stress, ', ',
                'Cycles: ', qfm.cycle_Endured, ', ',
                'Broken: ', qfd.is_broken
            ) AS fatigue_info,
            CONCAT(
                'Stress: ', qrm.stress, ', ',
                'Nature: ', qrm.stress_nature
            ) AS residual_info,
            CONCAT(
                'mpam_10: ', qfgm.mpam_10, ', ',
                'mpam_13: ', qfgm.mpam_13
            ) AS fcgr_info,
            CONCAT(
                'Parameter: ', qftm.parameter, ', ',
                'Value: ', qftm.value
            ) AS fracture_toughness_info,
            CONCAT(
                'Hardness Count: ', qclm.hardness_reading_count, ', ',
                'Mean RSH: ', qclm.mean_rsh, ', ',
                'Max Variation: ', qclm.max_variation
            ) AS centerline_info
        FROM
            qct_master AS qm
        INNER JOIN
            qct_duty AS qd ON qm.duty_id = qd.duty_id
        LEFT JOIN
            qct_fatigue_master AS qfm ON qd.duty_id = qfm.duty_id
        LEFT JOIN
            qct_fatigue_detail AS qfd ON qm.qct_id = qfd.qct_id and qd.duty_id = qfd.duty_id
        LEFT JOIN
            qct_residual_master AS qrm ON qm.qct_id = qrm.qct_id and qd.duty_id = qrm.duty_id
        LEFT JOIN
            qct_fcgr_master AS qfgm ON qm.qct_id = qfgm.qct_id and qd.duty_id = qfgm.duty_id
        LEFT JOIN
            qct_fracture_toughness_master AS qftm ON qm.qct_id = qftm.qct_id and qd.duty_id = qftm.duty_id
        LEFT JOIN
            qct_center_line_master AS qclm ON qm.qct_id = qclm.qct_id and qd.duty_id = qclm.duty_id
        WHERE
            DATE_FORMAT(qd.date, '%m-%Y') = :monthYear
        ORDER BY qm.qct_id
    """)
    List<Object[]> findQctRecord(@Param("monthYear") String monthYear);
}
