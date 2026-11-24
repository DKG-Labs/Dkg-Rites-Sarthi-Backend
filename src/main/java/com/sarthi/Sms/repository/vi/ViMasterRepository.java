package com.sarthi.Sms.repository.vi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sarthi.Sms.entity.visualInspection.ViMasterEntity;
@Repository
public interface ViMasterRepository extends JpaRepository<ViMasterEntity, String> {
    // Optional<ViMasterEntity> findByViId(ViId viId);

    @Query(value = """
        SELECT
            vid.date AS date,
            vid.shift AS shift,
            vid.mill AS inspectionWorkspot,
            vid.rail_grade AS railGrade,
            vid.rail_section AS railSection,
            vid.standard_offered_length AS stdOfferedLengthForInspection,
            COUNT(vim.rail_id) AS railsOfferedForInspection,

            -- Aggregated acceptance data by rail class and length (Class A)
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 130 THEN viad.accepted_number ELSE 0 END) AS classA130m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 117 THEN viad.accepted_number ELSE 0 END) AS classA117m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 104 THEN viad.accepted_number ELSE 0 END) AS classA104m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 86.67 THEN viad.accepted_number ELSE 0 END) AS classA86_67m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 65 THEN viad.accepted_number ELSE 0 END) AS classA65m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 52 THEN viad.accepted_number ELSE 0 END) AS classA52m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 26 THEN viad.accepted_number ELSE 0 END) AS classA26m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 25 THEN viad.accepted_number ELSE 0 END) AS classA25m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 24 THEN viad.accepted_number ELSE 0 END) AS classA24m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 18 THEN viad.accepted_number ELSE 0 END) AS classA18m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 17 THEN viad.accepted_number ELSE 0 END) AS classA17m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 16 THEN viad.accepted_number ELSE 0 END) AS classA16m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 13 THEN viad.accepted_number ELSE 0 END) AS classA13m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 12 THEN viad.accepted_number ELSE 0 END) AS classA12m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 11 THEN viad.accepted_number ELSE 0 END) AS classA11m,
            SUM(CASE WHEN viad.rail_class IN ('Class A', 'A') AND viad.accepted_length = 10 THEN viad.accepted_number ELSE 0 END) AS classA10m,

            -- Numeric aggregate for Class A + 0.1
            SUM(CASE WHEN viad.rail_class IN ('A + 0.1', 'Class A + 0.1') THEN viad.accepted_number ELSE 0 END) AS classAPlus01Total,

            -- Total accepted meters (A and A+0.1)
            SUM(CASE WHEN viad.rail_class IN ('Class A','A','A + 0.1','Class A + 0.1') THEN (viad.accepted_length * viad.accepted_number) ELSE 0 END) AS totalAcceptedMeters,

            -- Recycle data
            COUNT(CASE WHEN vim.ut_feedback = 'CutBar' THEN 1 ELSE NULL END) AS recycleCutBar,
            COUNT(CASE WHEN vim.ut_feedback = 'Refinish' THEN 1 ELSE NULL END) AS recycleRefinish,

            -- Rejection data
            SUM(vim.rej_13m) AS rejection13m,
            SUM(vim.rej_12m) AS rejection12m,
            SUM(vim.rej_11m) AS rejection11m,
            SUM(vim.rej_10m) AS rejection10m,
            SUM(vim.rej_comp_length) AS rejectionComponentLength

        FROM
            visual_inspection_duty vid
        LEFT JOIN
            visual_inspection_master vim ON vid.duty_id = vim.duty_id
        LEFT JOIN
            visual_inspection_accept_data viad ON vim.rail_id = viad.rail_id

        WHERE
            vid.date BETWEEN :startDate AND :endDate
            AND (:lineNo IS NULL OR vid.line_no = :lineNo)

        GROUP BY
            vid.date, vid.shift, vid.mill, vid.rail_grade, vid.rail_section, vid.standard_offered_length
        """, nativeQuery = true)
    List<Object[]> getActRejReport(@Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate,
    @Param("lineNo") String lineNo);

    @Query(value = """
        SELECT 
            vim.rail_id AS Rail_ID,
            vim.heat_number AS Heat_Number,
            CONCAT(DATE_FORMAT(vid.date, '%d.%m.%Y'), ' & ', vid.shift) AS Date_Shift,
            vid.rail_grade AS Rail_Grade,
            vid.rail_section AS Rail_Section,
            CONCAT(vid.mill, ' - ', vid.line_no) AS Inspection_Workspot, 
            vid.standard_offered_length AS Standard_Offered_Length,
            vim.actual_offered_length AS Actual_Offered_Length,
            GROUP_CONCAT(DISTINCT
                CASE
                    WHEN viad.rail_class = 'A' THEN CONCAT(FORMAT(viad.accepted_length, 2), ' x ', viad.accepted_number)
                    ELSE NULL
                END
                SEPARATOR '\n'
            ) AS Acceptance_in_Class_A,
            GROUP_CONCAT(DISTINCT
                CASE
                    WHEN viad.rail_class = 'A + 0.1' THEN CONCAT(FORMAT(viad.accepted_length, 2), ' x ', viad.accepted_number)
                    ELSE NULL
                END
                SEPARATOR '\n'
            ) AS Acceptance_in_Class_A_Plus_0_1,
            GROUP_CONCAT(DISTINCT
                CASE
                    WHEN viad.rail_class = 'B' THEN CONCAT(FORMAT(viad.accepted_length, 2), ' x ', viad.accepted_number)
                    ELSE NULL
                END
                SEPARATOR '\n'
            ) AS Acceptance_in_Class_B,
            GROUP_CONCAT(DISTINCT vis.sub_rail_id SEPARATOR '\n') AS Accepted_IDs,
            CONCAT(
                CASE WHEN vim.rej_13m > 0 THEN CONCAT(vim.rej_13m, ' x 13m') ELSE '' END,
                CASE WHEN vim.rej_12m > 0 AND vim.rej_13m > 0 THEN ', ' WHEN vim.rej_12m > 0 THEN '' ELSE '' END,
                CASE WHEN vim.rej_12m > 0 THEN CONCAT(vim.rej_12m, ' x 12m') ELSE '' END,
                CASE WHEN vim.rej_11m > 0 AND (vim.rej_13m > 0 OR vim.rej_12m > 0) THEN ', ' WHEN vim.rej_11m > 0 THEN '' ELSE '' END,
                CASE WHEN vim.rej_11m > 0 THEN CONCAT(vim.rej_11m, ' x 11m') ELSE '' END,
                CASE WHEN vim.rej_10m > 0 AND (vim.rej_13m > 0 OR vim.rej_12m > 0 OR vim.rej_11m > 0) THEN ', ' WHEN vim.rej_10m > 0 THEN '' ELSE '' END,
                CASE WHEN vim.rej_10m > 0 THEN CONCAT(vim.rej_10m, ' x 10m') ELSE '' END,
                CASE WHEN vim.rej_comp_length > 0 AND (vim.rej_13m > 0 OR vim.rej_12m > 0 OR vim.rej_11m > 0 OR vim.rej_10m > 0) THEN ', ' WHEN vim.rej_comp_length > 0 THEN '' ELSE '' END,
                CASE WHEN vim.rej_comp_length > 0 THEN CONCAT(FORMAT(vim.rej_comp_length, 2), 'm') ELSE '' END
            ) AS Rejected_Length,
            GROUP_CONCAT(DISTINCT CONCAT(vidf.defect_category, ': ', vidf.defect_type) SEPARATOR '\n') AS Reason_for_Defect
        FROM 
            visual_inspection_master vim
        LEFT JOIN 
            visual_inspection_duty vid 
            ON vim.duty_id = vid.duty_id
        LEFT JOIN 
            visual_inspection_accept_data viad 
            ON vim.rail_id = viad.rail_id
        LEFT JOIN 
            visual_inspection_sub_rail_id vis 
            ON vim.rail_id = vis.rail_id
        LEFT JOIN 
            visual_inspection_defect_data vidf 
            ON vim.rail_id = vidf.rail_id
        WHERE
            vid.date BETWEEN :startDate AND :endDate
        GROUP BY
            vim.rail_id, vim.heat_number, vid.date, vid.shift, vid.rail_grade, vid.rail_section,
            vid.mill, vid.line_no, vid.standard_offered_length, vim.actual_offered_length
        """, nativeQuery = true)
    List<Object[]> getAcptDataReport(@Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate);

    @Query(value = """
        SELECT
            vid.date AS date,
            vid.line_no AS inspectionWorkspot,
            vid.rail_section AS railSection,
            vid.rail_grade AS railGrade,
            vid.standard_offered_length AS stdOfferedLengthForInspection,
            COUNT(vim.rail_id) AS noOfRailsOfferedForInspection,
            SUM(CASE WHEN vim.defect_type LIKE '%NMI%' THEN 1 ELSE 0 END) AS nmi,
            SUM(CASE WHEN vim.defect_type LIKE '%MDF%' THEN 1 ELSE 0 END) AS mdf,
            SUM(CASE WHEN vim.defect_type LIKE '%MDM%' THEN 1 ELSE 0 END) AS mdm,
            SUM(CASE WHEN vim.defect_type LIKE '%HS%' THEN 1 ELSE 0 END) AS hs,
            SUM(CASE WHEN vim.defect_type LIKE '%LS%' THEN 1 ELSE 0 END) AS ls,
            SUM(CASE WHEN vim.defect_type LIKE '%HH%' THEN 1 ELSE 0 END) AS hh,
            SUM(CASE WHEN vim.defect_type LIKE '%LH%' THEN 1 ELSE 0 END) AS lh,
            SUM(CASE WHEN vim.defect_type LIKE '%NF%' THEN 1 ELSE 0 END) AS nf,
            SUM(CASE WHEN vim.defect_type LIKE '%WF%' THEN 1 ELSE 0 END) AS wf,
            SUM(CASE WHEN vim.defect_type LIKE '%OHT%' THEN 1 ELSE 0 END) AS oht,
            SUM(CASE WHEN vim.defect_type LIKE '%UHT%' THEN 1 ELSE 0 END) AS uht,
            SUM(CASE WHEN vim.defect_type LIKE '%TNW%' THEN 1 ELSE 0 END) AS tnw,
            SUM(CASE WHEN vim.defect_type LIKE '%TKW%' THEN 1 ELSE 0 END) AS tkw,
            SUM(CASE WHEN vim.defect_type LIKE '%LAP%' THEN 1 ELSE 0 END) AS lap,
            SUM(CASE WHEN vim.defect_type LIKE '%GM%' THEN 1 ELSE 0 END) AS gm,
            SUM(CASE WHEN vim.defect_type LIKE '%BR%' THEN 1 ELSE 0 END) AS br,
            SUM(CASE WHEN vim.defect_type LIKE '%KK%' THEN 1 ELSE 0 END) AS kk,
            SUM(CASE WHEN vim.defect_type LIKE '%TW%' THEN 1 ELSE 0 END) AS tw,
            SUM(CASE WHEN vim.defect_type LIKE '%C/RC%' THEN 1 ELSE 0 END) AS cRc,
            SUM(CASE WHEN vim.defect_type LIKE '%NS%' THEN 1 ELSE 0 END) AS ns,
            SUM(CASE WHEN vim.defect_type LIKE '%OL%' THEN 1 ELSE 0 END) AS ol,
            SUM(CASE WHEN vim.defect_type LIKE '%SL%' THEN 1 ELSE 0 END) AS sl,
            SUM(CASE WHEN vim.defect_type LIKE '%OS%' THEN 1 ELSE 0 END) AS os,
            SUM(CASE WHEN vim.defect_type LIKE '%NHN%' THEN 1 ELSE 0 END) AS nhn,
            SUM(CASE WHEN vim.defect_type LIKE '%ASY%' THEN 1 ELSE 0 END) AS asy,
            SUM(CASE WHEN vim.defect_type LIKE '%BE%' THEN 1 ELSE 0 END) AS be,
            SUM(CASE WHEN vim.defect_type LIKE '%US%' THEN 1 ELSE 0 END) AS us,
            SUM(CASE WHEN vim.defect_type LIKE '%DS%' THEN 1 ELSE 0 END) AS ds,
            SUM(CASE WHEN vim.defect_type LIKE '%UT%' THEN 1 ELSE 0 END) AS ut,
            SUM(CASE WHEN vim.defect_type LIKE '%TF(%)%' THEN 1 ELSE 0 END) AS tfPositive,
            SUM(CASE WHEN vim.defect_type LIKE '%TF(-)%' THEN 1 ELSE 0 END) AS tfNegative,
            SUM(CASE WHEN vim.defect_type LIKE '%HF(%)%' THEN 1 ELSE 0 END) AS hfPositive,
            SUM(CASE WHEN vim.defect_type LIKE '%HF(-)%' THEN 1 ELSE 0 END) AS hfNegative,
            SUM(CASE WHEN vim.defect_type LIKE '%CP%' THEN 1 ELSE 0 END) AS cp,
            SUM(CASE WHEN vim.defect_type LIKE '%FBC%' THEN 1 ELSE 0 END) AS fbc,
            SUM(CASE WHEN vim.defect_type LIKE '%FBCX%' THEN 1 ELSE 0 END) AS fbcx,
            SUM(CASE WHEN vim.defect_type LIKE '%Heat Reject(HR)%' OR vim.defect_type LIKE '%HR%' THEN 1 ELSE 0 END) AS hr,
            SUM(CASE WHEN vim.defect_type LIKE '%others%' THEN 1 ELSE 0 END) AS others,
            COUNT(vim.rail_id) AS total,
            vid.date AS dateForSteelMillFinishUT,
            vid.mill AS mill,
            MAX(vm.heat_status) AS finish,
            SUM(CASE WHEN vim.defect_type LIKE '%UT%' THEN 1 ELSE 0 END) AS utStandardOfferedLength
        FROM visual_inspection_duty vid
        join visual_inspection_master vm on vm.duty_id = vid.duty_id
        JOIN visual_inspection_defect_data vim ON vm.rail_id = vim.rail_id
        WHERE vid.date BETWEEN :startDate AND :endDate
        GROUP BY vid.date, vid.line_no, vid.rail_section, vid.rail_grade, vid.standard_offered_length, vid.mill
        ORDER BY vid.date, vid.line_no
    """, nativeQuery = true)
    List<Object[]> findViDefectAnalysisRecord(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = """
        SELECT
            vim.heat_number,
            vim.heat_status,
            vim.rail_id,
            vim.gauging_end_straightness,
            vim.ut_feedback,
            CASE
                WHEN vid.defect_type IN ('US', 'DS', 'OS') THEN 'YES'
                ELSE 'NO'
            END AS defectPresent,
            vim.actual_offered_length,
            GROUP_CONCAT(DISTINCT
                CASE
                    WHEN via.rail_class = 'A' THEN CONCAT(FORMAT(via.accepted_length, 2), ' x ', via.accepted_number)
                    ELSE NULL
                END
                SEPARATOR ', '
            ) AS acceptanceClassA,
            GROUP_CONCAT(DISTINCT
                CASE
                    WHEN via.rail_class = 'A + 0.1' THEN CONCAT(FORMAT(via.accepted_length, 2), ' x ', via.accepted_number)
                    ELSE NULL
                END
                SEPARATOR ', '
            ) AS acceptanceClassAPlus,
            CONCAT_WS(', ',
                CASE WHEN vim.rej_13m > 0 THEN CONCAT('13m x ', vim.rej_13m) ELSE NULL END,
                CASE WHEN vim.rej_12m > 0 THEN CONCAT('12m x ', vim.rej_12m) ELSE NULL END,
                CASE WHEN vim.rej_11m > 0 THEN CONCAT('11m x ', vim.rej_11m) ELSE NULL END,
                CASE WHEN vim.rej_10m > 0 THEN CONCAT('10m x ', vim.rej_10m) ELSE NULL END,
                CASE WHEN vim.rej_comp_length > 0 THEN CONCAT(FORMAT(vim.rej_comp_length, 2), 'm x 1') ELSE NULL END
            ) AS rejectedLengthDetails,
            GROUP_CONCAT(DISTINCT CONCAT(vid.defect_category, ': ', vid.defect_type) SEPARATOR ', ') AS defectAnalysis,
            vim.remarks
        FROM visual_inspection_master vim
        JOIN visual_inspection_duty viduty ON vim.duty_id = viduty.duty_id
        LEFT JOIN visual_inspection_defect_data vid ON vim.rail_id = vid.rail_id
        LEFT JOIN visual_inspection_accept_data via ON vim.rail_id = via.rail_id
        WHERE viduty.date = :date
        AND viduty.shift = :shift
        AND viduty.rail_grade = :railGrade
        AND viduty.rail_section = :railSection
        GROUP BY vim.heat_number, vim.heat_status, vim.rail_id, vim.gauging_end_straightness,
                 vim.ut_feedback, defectPresent, vim.actual_offered_length, vim.remarks
        """, nativeQuery = true)
    List<Object[]> findViOnlineInspDtl(
        @Param("date") LocalDate date,
        @Param("shift") String shift,
        @Param("railGrade") String railGrade,
        @Param("railSection") String railSection
    );

    List<ViMasterEntity> findByDutyId(String dutyId);

    // New methods for date and shift-based filtering
    @Query(value = """
        SELECT vim.* FROM visual_inspection_master vim
        JOIN visual_inspection_duty vid ON vim.duty_id = vid.duty_id
        WHERE vid.date = :date AND vid.shift = :shift
        """, nativeQuery = true)
    List<ViMasterEntity> findByDateAndShift(@Param("date") LocalDate date, @Param("shift") String shift);

    @Query(value = """
        SELECT vim.* FROM visual_inspection_master vim
        JOIN visual_inspection_duty vid ON vim.duty_id = vid.duty_id
        WHERE vid.date = :date AND vid.shift = :shift AND vid.line_no = :lineNumber
        """, nativeQuery = true)
    List<ViMasterEntity> findByDateShiftAndLine(@Param("date") LocalDate date, @Param("shift") String shift, @Param("lineNumber") Integer lineNumber);

    @Query(value = """
        SELECT vim.* FROM visual_inspection_master vim
        JOIN visual_inspection_duty vid ON vim.duty_id = vid.duty_id
        WHERE vid.date = :date AND vid.shift = :shift AND vid.mill = :mill
        """, nativeQuery = true)
    List<ViMasterEntity> findByDateShiftAndMill(@Param("date") LocalDate date, @Param("shift") String shift, @Param("mill") String mill);

    @Query(value = """
        SELECT vim.* FROM visual_inspection_master vim
        JOIN visual_inspection_duty vid ON vim.duty_id = vid.duty_id
        WHERE vid.date = :date AND vid.shift = :shift AND vid.line_no = :lineNumber AND vid.mill = :mill
        """, nativeQuery = true)
    List<ViMasterEntity> findByDateShiftLineAndMill(@Param("date") LocalDate date, @Param("shift") String shift, @Param("lineNumber") Integer lineNumber, @Param("mill") String mill);
    // Optional<ViMasterEntity> findById(@Param("railId") String railId);
}
