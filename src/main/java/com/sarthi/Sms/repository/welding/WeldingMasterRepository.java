package com.sarthi.Sms.repository.welding;

import com.sarthi.Sms.entity.welding.WeldingDetailEntity;
import com.sarthi.Sms.entity.welding.WeldingMasterEntity;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface WeldingMasterRepository extends JpaRepository<WeldingMasterEntity, Integer> {

    Optional<WeldingMasterEntity> findByPanelId(String panelId);

    Optional<WeldingMasterEntity> findByInspectionMasterId(Integer inspectionMasterId);

    @Query(value = """
        SELECT 
            wid.date AS "Date",
            wid.shift AS "Shift",
            CONCAT(wid.mill, ' - ', wid.welding_line) AS "Inspection Workspot",
            wid.rail_section AS "Rail Section",
            wid.rail_grade AS "Rail Grade",
            wim.panel_id AS "Panel ID",
            widetail.joint_number AS "Joint Number",
            widetail.result AS "Weld Joint Inspection(Result)",
            CASE 
                WHEN widetail.result != 'OK' THEN 
                    CONCAT(COALESCE(widetail.dimension_detail, ''), 
                           COALESCE(widetail.usfd_detail, ''), 
                           ' ', COALESCE(widetail.remark, ''))
                ELSE NULL 
            END AS "Weld Joint Inspection(Reason, if rejected)",
            widetail.remark AS "Weld Joint Inspection(Remark)",
            widetail.rail_id_1 AS "Rail IDs used for weld Joint(Rail ID 1)",
            widetail.rail_id_2 AS "Rail IDs used for weld Joint(Rail ID 2)",
            wim.panel_length AS "Panel Length",
            wim.panel_decision AS "Panel Decision",
            wim.panel_remark AS "Remark",
            widetail.rail_id_1_length AS "Welding Rail Length 1",
            widetail.rail_id_2_length AS "Welding Rail Length 2"
        FROM 
            welding_inspection_duty wid
        INNER JOIN 
            welding_duty_inspection_relation wdir ON wid.duty_id = wdir.duty_id
        INNER JOIN 
            welding_inspection_master wim ON wdir.inspection_master_id = wim.inspection_master_id
        INNER JOIN 
            welding_inspection_detail widetail ON wim.inspection_master_id = widetail.inspection_master_id
        WHERE 
            wid.date BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    List<Object[]> getNewWeldingReport(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);


        //                 @Query(value = """
        // SELECT 
        //     wid.date AS date,
        //     wid.rail_section AS railSection,
        //     wid.rail_grade AS railGrade,
        //     wid.shift_remarks AS inspectionWorkspot,
        //     wt.machine_number AS weldingMachineNumber,
        //     SUM(wim.number_of_joints) AS numberOfJointsOfferedForInspection,
        //     COUNT(DISTINCT wim.panel_id) AS numberOfPanelsOfferedForInspection,
        //     SUM(CASE WHEN wid.result = 'Accepted' AND wid.dimension = TRUE THEN 1 ELSE 0 END) AS dimensionalCheckAccepted,
        //     SUM(CASE WHEN wid.result = 'Rejected' AND wid.dimension = TRUE THEN 1 ELSE 0 END) AS dimensionalCheckRejected,
        //     SUM(CASE WHEN wid.result = 'Accepted' AND wid.usfd = TRUE THEN 1 ELSE 0 END) AS utCheckAccepted,
        //     SUM(CASE WHEN wid.result = 'Rejected' AND wid.usfd = TRUE THEN 1 ELSE 0 END) AS utCheckRejected,
        //     SUM(CASE WHEN wid.result = 'Accepted' THEN 1 ELSE 0 END) AS numberOfJointsAccepted,
        //     SUM(CASE WHEN wid.result = 'Rejected' THEN 1 ELSE 0 END) AS numberOfJointsRejected,
        //     SUM(CASE WHEN wim.panel_decision = 'Accepted' THEN 1 ELSE 0 END) AS numberOfPanelsAccepted,
        //     SUM(CASE WHEN wim.panel_decision = 'Rejected' THEN 1 ELSE 0 END) AS numberOfPanelsRejected,
        //     wid.rail_id_1_length AS railLength
        // FROM 
        //     welding_inspection_duty wid
        // LEFT JOIN 
        //     welding_duty_inspection_relation widr ON wid.duty_id = widr.duty_id
        // LEFT JOIN 
        //     welding_inspection_master wim ON widr.inspection_master_id = wim.inspection_master_id
        // LEFT JOIN 
        //     welding_inspection_detail wid ON wid.inspection_master_id = wim.inspection_master_id
        // LEFT JOIN 
        //     welding_tlt wt ON wt.duty_id = wid.duty_id
        // WHERE 
        //     wid.date BETWEEN :startDate AND :endDate
        // GROUP BY 
        //     wid.date, wid.rail_section, wid.rail_grade, wid.shift_remarks, wt.machine_number
        // ORDER BY 
        //     wid.date
        // """, nativeQuery = true)

        @Query(value = """
    SELECT 
        wid.date AS date,
        wid.rail_section AS railSection,
        wid.rail_grade AS railGrade,
        wid.shift_remarks AS inspectionWorkspot,
        wt.machine_number AS weldingMachineNumber,
        SUM(wim.number_of_joints) AS numberOfJointsOfferedForInspection,
        COUNT(DISTINCT wim.panel_id) AS numberOfPanelsOfferedForInspection,
        SUM(CASE WHEN widetail.result = 'Accepted' AND widetail.dimension = TRUE THEN 1 ELSE 0 END) AS dimensionalCheckAccepted,
        SUM(CASE WHEN widetail.result = 'Rejected' AND widetail.dimension = TRUE THEN 1 ELSE 0 END) AS dimensionalCheckRejected,
        SUM(CASE WHEN widetail.result = 'Accepted' AND widetail.usfd = TRUE THEN 1 ELSE 0 END) AS utCheckAccepted,
        SUM(CASE WHEN widetail.result = 'Rejected' AND widetail.usfd = TRUE THEN 1 ELSE 0 END) AS utCheckRejected,
        SUM(CASE WHEN widetail.result = 'Accepted' THEN 1 ELSE 0 END) AS numberOfJointsAccepted,
        SUM(CASE WHEN widetail.result = 'Rejected' THEN 1 ELSE 0 END) AS numberOfJointsRejected,
        SUM(CASE WHEN wim.panel_decision = 'Accepted' THEN 1 ELSE 0 END) AS numberOfPanelsAccepted,
        SUM(CASE WHEN wim.panel_decision = 'Rejected' THEN 1 ELSE 0 END) AS numberOfPanelsRejected,
        widetail.rail_id_1_length AS railLength
    FROM 
        welding_inspection_duty wid
    LEFT JOIN 
        welding_duty_inspection_relation widr ON wid.duty_id = widr.duty_id
    LEFT JOIN 
        welding_inspection_master wim ON widr.inspection_master_id = wim.inspection_master_id
    LEFT JOIN 
        welding_inspection_detail widetail ON widetail.inspection_master_id = wim.inspection_master_id
    LEFT JOIN 
        welding_tlt wt ON wt.duty_id = wid.duty_id
    WHERE 
        wid.date BETWEEN :startDate AND :endDate
    GROUP BY 
        wid.date, wid.rail_section, wid.rail_grade, wid.shift_remarks, wt.machine_number, widetail.rail_id_1_length
    ORDER BY 
        wid.date
    """, nativeQuery = true)
    List<Object[]> getWeldingSummaryReport(@Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate);

    @Query(value = """
    SELECT 
        wid.joint_number,
        MAX(CASE WHEN wid.dimension = 'LH' THEN 'YES' ELSE 'NO' END) AS LH,
        MAX(CASE WHEN wid.dimension = '1m RH' THEN 'YES' ELSE 'NO' END) AS `1m RH`,
        MAX(CASE WHEN wid.dimension = '1m Vertical' THEN 'YES' ELSE 'NO' END) AS `1m Vertical`,
        MAX(CASE WHEN wid.dimension = '10cm LH' THEN 'YES' ELSE 'NO' END) AS `10cm LH`,
        MAX(CASE WHEN wid.dimension = '10cm RH' THEN 'YES' ELSE 'NO' END) AS `10cm RH`,
        MAX(CASE WHEN wid.dimension = '10cm Vertical' THEN 'YES' ELSE 'NO' END) AS `10cm Vertical`,
        MAX(CASE WHEN wid.visual = TRUE THEN 'YES' ELSE 'NO' END) AS visual,
        MAX(CASE WHEN wid.marking = TRUE THEN 'YES' ELSE 'NO' END) AS marking,
        wid.remark
    FROM welding_inspection_detail wid
    JOIN welding_inspection_master wim ON wid.inspection_master_id = wim.inspection_master_id
    JOIN welding_inspection_duty widuty ON wim.panel_id = widuty.duty_id
    WHERE widuty.rail_grade = :railGrade
    AND widuty.rail_section = :railSection
    AND widuty.date = :date
    AND widuty.shift = :shift
    GROUP BY wid.joint_number, wid.remark
    """, nativeQuery = true)
List<Object[]> findWeldingInspDimensionalCheck(
    @Param("date") LocalDate date,
    @Param("shift") String shift,
    @Param("railGrade") String railGrade,
    @Param("railSection") String railSection
);

@Query(value = """
    SELECT 
        wid.joint_number,
        wid.weld_operator,
        wid.usfd_operator,
        wid.result,
        wid.remark,
        wim.panel_remark
    FROM welding_inspection_detail wid
    JOIN welding_inspection_master wim ON wid.inspection_master_id = wim.inspection_master_id
    JOIN welding_inspection_duty widuty ON wim.panel_id = widuty.duty_id
    WHERE widuty.rail_grade = :railGrade
    AND widuty.rail_section = :railSection
    AND widuty.date = :date
    AND widuty.shift = :shift
    """, nativeQuery = true)
List<Object[]> findJointTestingDtl(
    @Param("date") LocalDate date,
    @Param("shift") String shift,
    @Param("railGrade") String railGrade,
    @Param("railSection") String railSection
);

    @Query(value = "SELECT wm FROM WeldingMasterEntity wm WHERE wm.dutyId = :dutyId")
    List<WeldingMasterEntity> findByDutyId(@Param("dutyId") String dutyId);

    @Query(value = "SELECT wd FROM WeldingDetailEntity wd WHERE wd.inspectionMasterId = :inspectionMasterId")
    List<WeldingDetailEntity> findDetailsByInspectionMasterId(@Param("inspectionMasterId") Integer inspectionMasterId);

}
