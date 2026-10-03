package com.sarthi.repository.processmaterial;

import com.sarthi.dto.summaryDtos.PlantShiftWiseRawDto;
import com.sarthi.entity.processmaterial.ProcessLineFinalResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ProcessLineFinalResult entity.
 */
@Repository
public interface ProcessLineFinalResultRepository extends JpaRepository<ProcessLineFinalResult, Long> {

    /**
     * Find all final results for a specific inspection call.
     */
    List<ProcessLineFinalResult> findByInspectionCallNo(String inspectionCallNo);

    List<ProcessLineFinalResult> findByInspectionCallNoAndDateOfInspectionAndShift(
            String inspectionCallNo, java.time.LocalDate dateOfInspection, String shift);

    List<ProcessLineFinalResult> findByInspectionCallNoAndDateOfInspectionAndShiftAndCreatedBy(
            String inspectionCallNo, java.time.LocalDate dateOfInspection, String shift, String createdBy);

    List<ProcessLineFinalResult> findByInspectionCallNoAndShiftAndLotNumberAndLineNo(
            String inspectionCallNo, String shift, String lotNumber, String lineNo);

    List<ProcessLineFinalResult> findByInspectionCallNoAndShiftAndLotNumberAndLineNoAndCreatedBy(
            String inspectionCallNo, String shift, String lotNumber, String lineNo, String createdBy);

    /**
     * Find final result for a specific inspection call and line number.
     */
    Optional<ProcessLineFinalResult> findByInspectionCallNoAndLineNo(String inspectionCallNo, String lineNo);

    /**
     * Find the most recent final result for a specific inspection call and line
     * number.
     */
    Optional<ProcessLineFinalResult> findFirstByInspectionCallNoAndLineNoOrderByCreatedAtDesc(String inspectionCallNo,
            String lineNo);

    /**
     * Find all final results for a specific PO.
     */
    List<ProcessLineFinalResult> findByPoNo(String poNo);

    /**
     * Delete all final results for a specific inspection call.
     */
    void deleteByInspectionCallNo(String inspectionCallNo);

    /**
     * Get the sum of accepted quantities across all stages for a specific call and
     * lot.
     */
    @Query("""
                SELECT new com.sarthi.dto.processmaterial.ProcessStageAcceptedQtyDto(
                    p.inspectionCallNo,
                    p.lotNumber,
                    SUM(CAST(COALESCE(p.shearingManufactured, 0) AS long)),
                    SUM(CAST(COALESCE(p.shearingAccepted, 0) AS long)),
                    SUM(CAST(COALESCE(p.turningManufactured, 0) AS long)),
                    SUM(CAST(COALESCE(p.turningAccepted, 0) AS long)),
                    SUM(CAST(COALESCE(p.mpiManufactured, 0) AS long)),
                    SUM(CAST(COALESCE(p.mpiAccepted, 0) AS long)),
                    SUM(CAST(COALESCE(p.forgingManufactured, 0) AS long)),
                    SUM(CAST(COALESCE(p.forgingAccepted, 0) AS long)),
                    SUM(CAST(COALESCE(p.quenchingManufactured, 0) AS long)),
                    SUM(CAST(COALESCE(p.quenchingAccepted, 0) AS long)),
                    SUM(CAST(COALESCE(p.temperingManufactured, 0) AS long)),
                    SUM(CAST(COALESCE(p.temperingAccepted, 0) AS long))
                )
                FROM ProcessLineFinalResult p
                WHERE p.inspectionCallNo = :callNo AND p.lotNumber = :lotNo
                GROUP BY p.inspectionCallNo, p.lotNumber
            """)
    com.sarthi.dto.processmaterial.ProcessStageAcceptedQtyDto getSumOfAcceptedQuantitiesByCallAndLot(
            @org.springframework.data.repository.query.Param("callNo") String callNo,
            @org.springframework.data.repository.query.Param("lotNo") String lotNo);

    /*
     * @Query(value = """
     * SELECT
     * COALESCE(SUM(pt.tempering_temperature_rejected),0) +
     * COALESCE(SUM(pt.tempering_duration_rejected),0) +
     * 
     * COALESCE(SUM(pf.box_gauge_rejected),0) +
     * COALESCE(SUM(pf.flat_bearing_area_rejected),0) +
     * COALESCE(SUM(pf.falling_gauge_rejected),0) +
     * COALESCE(SUM(pf.surface_defect_rejected),0) +
     * COALESCE(SUM(pf.embossing_defect_rejected),0) +
     * COALESCE(SUM(pf.marking_rejected),0) +
     * COALESCE(SUM(pf.tempering_hardness_rejected),0) +
     * 
     * COALESCE(SUM(ptf.toe_load_rejected),0) +
     * COALESCE(SUM(ptf.weight_rejected),0) +
     * COALESCE(SUM(ptf.paint_identification_rejected),0) +
     * COALESCE(SUM(ptf.erc_coating_rejected),0)
     * 
     * FROM process_line_final_result pl
     * 
     * LEFT JOIN process_tempering_data pt
     * ON pt.inspection_call_no = pl.inspection_call_no
     * AND pt.lot_no = pl.lot_number
     * AND pt.shift = pl.shift
     * AND DATE(pt.created_at) = DATE(pl.created_at)
     * 
     * LEFT JOIN process_final_check_data pf
     * ON pf.inspection_call_no = pl.inspection_call_no
     * AND pf.lot_no = pl.lot_number
     * AND pf.shift = pl.shift
     * AND DATE(pf.created_at) = DATE(pl.created_at)
     * 
     * LEFT JOIN process_testing_finishing_data ptf
     * ON ptf.inspection_call_no = pl.inspection_call_no
     * AND ptf.lot_no = pl.lot_number
     * AND ptf.shift = pl.shift
     * AND DATE(ptf.created_at) = DATE(pl.created_at)
     * 
     * WHERE pl.inspection_call_no = :callId
     * AND pl.lot_number = :lotNumber
     * AND pl.shift = :shift
     * AND pl.created_at BETWEEN :startDate AND :endDate
     * """, nativeQuery = true)
     * Integer getTotalTemperingRejected(
     * String callId,
     * String lotNumber,
     * String shift,
     * LocalDateTime startDate,
     * LocalDateTime endDate
     * );
     */
    @Query(value = """
            SELECT
            (
                SELECT
                    COALESCE(SUM(tempering_temperature_rejected),0) +
                    COALESCE(SUM(tempering_duration_rejected),0)
                FROM process_tempering_data
                WHERE inspection_call_no = :callId
                  AND lot_no = :lotNumber
                  AND shift = :shift
                  AND created_at BETWEEN :startDate AND :endDate
            )
            +
            (
                SELECT
                    COALESCE(SUM(box_gauge_rejected),0) +
                    COALESCE(SUM(flat_bearing_area_rejected),0) +
                    COALESCE(SUM(falling_gauge_rejected),0) +
                    COALESCE(SUM(surface_defect_rejected),0) +
                    COALESCE(SUM(embossing_defect_rejected),0) +
                    COALESCE(SUM(marking_rejected),0) +
                    COALESCE(SUM(tempering_hardness_rejected),0)
                FROM process_final_check_data
                WHERE inspection_call_no = :callId
                  AND lot_no = :lotNumber
                  AND shift = :shift
                  AND created_at BETWEEN :startDate AND :endDate
            )
            +
            (
                SELECT
                    COALESCE(SUM(toe_load_rejected),0) +
                    COALESCE(SUM(weight_rejected),0) +
                    COALESCE(SUM(paint_identification_rejected),0) +
                    COALESCE(SUM(erc_coating_rejected),0)
                FROM process_testing_finishing_data
                WHERE inspection_call_no = :callId
                  AND lot_no = :lotNumber
                  AND shift = :shift
                  AND created_at BETWEEN :startDate AND :endDate
            )
            """, nativeQuery = true)
    Integer getTotalTemperingRejected(
            String callId,
            String lotNumber,
            String shift,
            LocalDateTime startDate,
            LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.shearingManufactured) FROM ProcessLineFinalResult p WHERE p.createdAt >= :date")
    Long sumShearingManufacturedLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @Query("""
            SELECT
                p.inspectionCallNo,
                SUM(p.totalManufactured),
                SUM(p.totalRejected)
            FROM ProcessLineFinalResult p
            WHERE p.inspectionCallNo IN :callNos
            GROUP BY p.inspectionCallNo
            """)
    List<Object[]> findProcessSummaryByCallNos(List<String> callNos);

    @Query("""
            SELECT
                SUM(p.totalManufactured),
                SUM(p.totalRejected)
            FROM ProcessLineFinalResult p
            WHERE p.inspectionCallNo IN :callNos
            GROUP BY p.inspectionCallNo
            """)
    List<Object[]> findProcessLineSummaryByCallNos(List<String> callNos);

    @Query("""
            SELECT
                p.inspectionCallNo,
                SUM(p.totalManufactured),
                SUM(p.totalRejected)
            FROM ProcessLineFinalResult p
            WHERE p.inspectionCallNo IN :callNos
            GROUP BY p.inspectionCallNo
            """)
    List<Object[]> findProcessLineSummaryByCallNosBatched(List<String> callNos);

    @Query("""
                SELECT
                    SUM(COALESCE(p.shearingRejected, 0) +
                        COALESCE(p.turningRejected, 0) +
                        COALESCE(p.mpiRejected, 0) +
                        COALESCE(p.forgingRejected, 0) +
                        COALESCE(p.quenchingRejected, 0) +
                        COALESCE(p.temperingRejected, 0)),
                    SUM(COALESCE(p.shearingManufactured, 0))
                FROM ProcessLineFinalResult p
                WHERE p.createdAt >= :date
            """)
    List<Object[]> sumProcessRejectionNewLogicLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @Query("""
                SELECT
                    SUM(COALESCE(p.totalRejected, 0)),
                    SUM(COALESCE(p.shearingManufactured, p.totalManufactured, 0))
                FROM ProcessLineFinalResult p
                WHERE p.createdAt >= :date
            """)
    List<Object[]> sumProcessRejectionRevisedLogicLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @Query(value = """
                SELECT
                    SUM(COALESCE(p.total_rejected, 0)),
                    SUM(COALESCE(p.shearing_manufactured, p.total_manufactured, 0))
                FROM process_line_final_result p
                LEFT JOIN inspection_calls ic ON p.inspection_call_no = ic.ic_number
                LEFT JOIN po_header ph ON ic.po_no = ph.po_no
                WHERE (CASE WHEN p.date_of_inspection IS NOT NULL THEN DATE(p.date_of_inspection) ELSE DATE(p.created_at) END) BETWEEN :startDate AND :endDate
                AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
                AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
            """, nativeQuery = true)
    List<Object[]> sumProcessRejectionRevisedLogicWithFilters(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
            @org.springframework.data.repository.query.Param("vendorPlantCode") String vendorPlantCode,
            @org.springframework.data.repository.query.Param("zonalRailway") String zonalRailway);

    @Query(value = """
                SELECT
                    ic.company_name AS name,
                    SUM(COALESCE(p.shearing_rejected, 0) +
                        COALESCE(p.turning_rejected, 0) +
                        COALESCE(p.mpi_rejected, 0) +
                        COALESCE(p.forging_rejected, 0) +
                        COALESCE(p.quenching_rejected, 0) +
                        COALESCE(p.tempering_rejected, 0)) * 100.0 /
                    NULLIF(SUM(COALESCE(p.shearing_manufactured, 0)), 0) AS rejectionPct
                FROM process_line_final_result p
                JOIN inspection_calls ic ON ic.ic_number = p.inspection_call_no
                WHERE p.created_at >= :date
                GROUP BY ic.company_name
                ORDER BY rejectionPct ASC
                LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findTop5ProcessPerformanceNewLogic(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @Query(value = """
                SELECT
                    ic.company_name AS name,
                    SUM(COALESCE(p.total_rejected, 0)) * 100.0 /
                    NULLIF(SUM(COALESCE(p.shearing_manufactured, 0)), 0) AS rejectionPct
                FROM process_line_final_result p
                JOIN inspection_calls ic ON ic.ic_number = p.inspection_call_no
                WHERE p.created_at >= :date
                GROUP BY ic.company_name
                ORDER BY rejectionPct ASC
                LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findTop5ProcessPerformanceRevisedLogic(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @Query(value = """
                SELECT
                    ic.company_name AS name,
                    SUM(COALESCE(p.total_rejected, 0)) * 100.0 /
                    NULLIF(SUM(COALESCE(p.shearing_manufactured, 0)), 0) AS rejectionPct
                FROM process_line_final_result p
                JOIN inspection_calls ic ON ic.ic_number = p.inspection_call_no
                WHERE p.created_at BETWEEN :startDate AND :endDate
                GROUP BY ic.company_name
                ORDER BY rejectionPct ASC
                LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findTop5ProcessPerformanceRevisedLogicBetweenDates(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
                SELECT
                    ic.company_name AS name,
                    SUM(COALESCE(p.shearing_rejected, 0) +
                        COALESCE(p.turning_rejected, 0) +
                        COALESCE(p.mpi_rejected, 0) +
                        COALESCE(p.forging_rejected, 0) +
                        COALESCE(p.quenching_rejected, 0) +
                        COALESCE(p.tempering_rejected, 0)) * 100.0 /
                    NULLIF(SUM(COALESCE(p.shearing_manufactured, 0)), 0) AS rejectionPct
                FROM process_line_final_result p
                JOIN inspection_calls ic ON ic.ic_number = p.inspection_call_no
                WHERE p.created_at >= :date
                GROUP BY ic.company_name
                ORDER BY rejectionPct DESC
                LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findWorst5ProcessPerformanceNewLogic(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @Query(value = """
                SELECT
                    ic.company_name AS name,
                    SUM(COALESCE(p.total_rejected, 0)) * 100.0 /
                    NULLIF(SUM(COALESCE(p.shearing_manufactured, 0)), 0) AS rejectionPct
                FROM process_line_final_result p
                JOIN inspection_calls ic ON ic.ic_number = p.inspection_call_no
                WHERE p.created_at >= :date
                GROUP BY ic.company_name
                ORDER BY rejectionPct DESC
                LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findWorst5ProcessPerformanceRevisedLogic(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @Query(value = """
                SELECT
                    ic.company_name AS name,
                    SUM(COALESCE(p.total_rejected, 0)) * 100.0 /
                    NULLIF(SUM(COALESCE(p.shearing_manufactured, 0)), 0) AS rejectionPct
                FROM process_line_final_result p
                JOIN inspection_calls ic ON ic.ic_number = p.inspection_call_no
                WHERE p.created_at BETWEEN :startDate AND :endDate
                GROUP BY ic.company_name
                ORDER BY rejectionPct DESC
                LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findWorst5ProcessPerformanceRevisedLogicBetweenDates(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
                SELECT
                    DATE_FORMAT(p.created_at, '%d-%b') AS displayDate,
                    SUM(COALESCE(p.shearing_rejected, 0) +
                        COALESCE(p.turning_rejected, 0) +
                        COALESCE(p.mpi_rejected, 0) +
                        COALESCE(p.forging_rejected, 0) +
                        COALESCE(p.quenching_rejected, 0) +
                        COALESCE(p.tempering_rejected, 0)) * 100.0 /
                    NULLIF(SUM(COALESCE(p.shearing_manufactured, p.total_manufactured, 0)), 0) AS rejectionPct
                FROM process_line_final_result p
                WHERE p.created_at BETWEEN :startDate AND :endDate
                GROUP BY DATE(p.created_at), DATE_FORMAT(p.created_at, '%d-%b')
                ORDER BY DATE(p.created_at) ASC
            """, nativeQuery = true)
    List<Object[]> findDailyRejectionTrendNewLogic(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
                SELECT
                    DATE_FORMAT(p.created_at, '%d-%b') AS displayDate,
                    SUM(COALESCE(p.total_rejected, 0)) * 100.0 /
                    NULLIF(SUM(COALESCE(p.shearing_manufactured, p.total_manufactured, 0)), 0) AS rejectionPct
                FROM process_line_final_result p
                WHERE p.created_at BETWEEN :startDate AND :endDate
                GROUP BY DATE(p.created_at), DATE_FORMAT(p.created_at, '%d-%b')
                ORDER BY DATE(p.created_at) ASC
            """, nativeQuery = true)
    List<Object[]> findDailyRejectionTrendRevisedLogic(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
                SELECT
                    SUM(COALESCE(p.shearing_rejected, 0)) AS shearing,
                    SUM(COALESCE(p.turning_rejected, 0)) AS turning,
                    SUM(COALESCE(p.mpi_rejected, 0)) AS mpi,
                    SUM(COALESCE(p.forging_rejected, 0)) AS forging,
                    SUM(COALESCE(p.quenching_rejected, 0)) AS quenching,
                    SUM(COALESCE(p.tempering_rejected, 0)) AS tempering
                FROM process_line_final_result p
                WHERE p.created_at >= :date
            """, nativeQuery = true)
    List<Object[]> sumStepWiseRejectionLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @Query(value = """
        SELECT
            SUM(COALESCE(p.shearing_rejected, 0)) AS shearing,
            SUM(COALESCE(p.turning_rejected, 0)) AS turning,
            SUM(COALESCE(p.mpi_rejected, 0)) AS mpi,
            SUM(COALESCE(p.forging_rejected, 0)) AS forging,
            SUM(COALESCE(p.quenching_rejected, 0)) AS quenching,
            SUM(COALESCE(p.tempering_rejected, 0)) AS tempering
        FROM process_line_final_result p
        WHERE p.created_at BETWEEN :startDate AND :endDate
        """, nativeQuery = true)
    List<Object[]> sumStepWiseRejection(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.temperingManufactured) FROM ProcessLineFinalResult p WHERE p.createdAt >= :date")
    Long sumTemperingManufacturedLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(DISTINCT DATE(p.created_at)) FROM process_line_final_result p WHERE p.created_at >= :date AND p.tempering_manufactured > 0", nativeQuery = true)
    Long countDistinctProductionDaysLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @org.springframework.data.jpa.repository.Query(value = """
                SELECT SUM(p.tempering_manufactured)
                FROM process_line_final_result p
                INNER JOIN inspection_calls ic ON p.inspection_call_no = ic.ic_number
                INNER JOIN po_header ph ON ic.po_no = ph.po_no
                INNER JOIN (
                    SELECT w.REQUESTID, w.STATUS
                    FROM WORKFLOW_TRANSITION w
                    INNER JOIN (
                        SELECT REQUESTID, MAX(WORKFLOWTRANSITIONID) AS max_id
                        FROM WORKFLOW_TRANSITION
                        GROUP BY REQUESTID
                    ) latest ON w.REQUESTID = latest.REQUESTID AND w.WORKFLOWTRANSITIONID = latest.max_id
                ) wf ON wf.REQUESTID = ic.ic_number
                WHERE (CASE WHEN p.date_of_inspection IS NOT NULL THEN DATE(p.date_of_inspection) ELSE DATE(p.created_at) END) BETWEEN :startDate AND :endDate
                AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
                AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
                AND wf.STATUS IN ('INSPECTION_COMPLETE_CONFIRM', 'GENERATE_IC', 'DSC_SIGN_IC')
            """, nativeQuery = true)
    Long sumTemperingManufacturedWithFilters(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
            @org.springframework.data.repository.query.Param("vendorPlantCode") String vendorPlantCode,
            @org.springframework.data.repository.query.Param("zonalRailway") String zonalRailway);

    @org.springframework.data.jpa.repository.Query(value = """
                SELECT COUNT(DISTINCT DATE(p.created_at))
                FROM process_line_final_result p
                INNER JOIN inspection_calls ic ON p.inspection_call_no = ic.ic_number
                INNER JOIN po_header ph ON ic.po_no = ph.po_no
                INNER JOIN (
                    SELECT w.REQUESTID, w.STATUS
                    FROM WORKFLOW_TRANSITION w
                    INNER JOIN (
                        SELECT REQUESTID, MAX(WORKFLOWTRANSITIONID) AS max_id
                        FROM WORKFLOW_TRANSITION
                        GROUP BY REQUESTID
                    ) latest ON w.REQUESTID = latest.REQUESTID AND w.WORKFLOWTRANSITIONID = latest.max_id
                ) wf ON wf.REQUESTID = ic.ic_number
                WHERE (CASE WHEN p.date_of_inspection IS NOT NULL THEN DATE(p.date_of_inspection) ELSE DATE(p.created_at) END) BETWEEN :startDate AND :endDate
                AND p.tempering_manufactured > 0
                AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
                AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
                AND wf.STATUS IN ('INSPECTION_COMPLETE_CONFIRM', 'GENERATE_IC', 'DSC_SIGN_IC')
            """, nativeQuery = true)
    Long countDistinctProductionDaysWithFilters(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
            @org.springframework.data.repository.query.Param("vendorPlantCode") String vendorPlantCode,
            @org.springframework.data.repository.query.Param("zonalRailway") String zonalRailway);

    // ===== NEW: Pareto Analysis – aggregate rejections across all process tables
    // =====
    @org.springframework.data.jpa.repository.Query(value = """
            SELECT 'Forging Temp' AS param_name, COALESCE(SUM(forging_temp_rejected), 0) AS total FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Forging Stabilisation', COALESCE(SUM(forging_stabilisation_rejection_rejected), 0) FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Improper Forging', COALESCE(SUM(improper_forging_rejected), 0) FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Forging Defect', COALESCE(SUM(forging_defect_rejected), 0) FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Embossing Defect', COALESCE(SUM(embossing_defect_rejected), 0) FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'MPI', COALESCE(SUM(mpi_rejected), 0) FROM process_mpi_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Length Cut Bar', COALESCE(SUM(length_cut_bar_rejected), 0) FROM process_shearing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Improper Dia', COALESCE(SUM(improper_dia_rejected), 0) FROM process_shearing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Sharp Edges', COALESCE(SUM(sharp_edges_rejected), 0) FROM process_shearing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Cracked Edges', COALESCE(SUM(cracked_edges_rejected), 0) FROM process_shearing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Parallel Length', COALESCE(SUM(parallel_length_rejected), 0) FROM process_turning_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Full Turning Length', COALESCE(SUM(full_turning_length_rejected), 0) FROM process_turning_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Turning Dia', COALESCE(SUM(turning_dia_rejected), 0) FROM process_turning_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL 
            SELECT 'Quenching Hardness', COALESCE(SUM(quenching_hardness_rejected), 0) FROM process_quenching_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Box Gauge (Quenching)', COALESCE(SUM(box_gauge_rejected), 0) FROM process_quenching_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Flat Bearing Area (Quenching)', COALESCE(SUM(flat_bearing_area_rejected), 0) FROM process_quenching_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Falling Gauge (Quenching)', COALESCE(SUM(falling_gauge_rejected), 0) FROM process_quenching_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Tempering Temperature', COALESCE(SUM(tempering_temperature_rejected), 0) FROM process_tempering_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Tempering Duration', COALESCE(SUM(tempering_duration_rejected), 0) FROM process_tempering_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Box Gauge (Final)', COALESCE(SUM(box_gauge_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Flat Bearing Area (Final)', COALESCE(SUM(flat_bearing_area_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Falling Gauge (Final)', COALESCE(SUM(falling_gauge_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Surface Defect', COALESCE(SUM(surface_defect_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Embossing (Final)', COALESCE(SUM(embossing_defect_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Marking', COALESCE(SUM(marking_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Tempering Hardness', COALESCE(SUM(tempering_hardness_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Toe Load', COALESCE(SUM(toe_load_rejected), 0) FROM process_testing_finishing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Weight', COALESCE(SUM(weight_rejected), 0) FROM process_testing_finishing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Paint Identification', COALESCE(SUM(paint_identification_rejected), 0) FROM process_testing_finishing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'ERC Coating', COALESCE(SUM(erc_coating_rejected), 0) FROM process_testing_finishing_data WHERE created_at BETWEEN :startDate AND :endDate
            ORDER BY total DESC
            LIMIT 10
            """, nativeQuery = true)
    java.util.List<Object[]> getParetoAnalysisRejections(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    @Query(value = """
    SELECT SUM(total)
    FROM (
     SELECT 'Forging Temp' AS param_name, COALESCE(SUM(forging_temp_rejected), 0) AS total FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Forging Stabilisation', COALESCE(SUM(forging_stabilisation_rejection_rejected), 0) FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Improper Forging', COALESCE(SUM(improper_forging_rejected), 0) FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Forging Defect', COALESCE(SUM(forging_defect_rejected), 0) FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Embossing Defect', COALESCE(SUM(embossing_defect_rejected), 0) FROM process_forging_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'MPI', COALESCE(SUM(mpi_rejected), 0) FROM process_mpi_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Length Cut Bar', COALESCE(SUM(length_cut_bar_rejected), 0) FROM process_shearing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Improper Dia', COALESCE(SUM(improper_dia_rejected), 0) FROM process_shearing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Sharp Edges', COALESCE(SUM(sharp_edges_rejected), 0) FROM process_shearing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Cracked Edges', COALESCE(SUM(cracked_edges_rejected), 0) FROM process_shearing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Parallel Length', COALESCE(SUM(parallel_length_rejected), 0) FROM process_turning_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Full Turning Length', COALESCE(SUM(full_turning_length_rejected), 0) FROM process_turning_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Turning Dia', COALESCE(SUM(turning_dia_rejected), 0) FROM process_turning_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL 
            SELECT 'Quenching Hardness', COALESCE(SUM(quenching_hardness_rejected), 0) FROM process_quenching_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Box Gauge (Quenching)', COALESCE(SUM(box_gauge_rejected), 0) FROM process_quenching_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Flat Bearing Area (Quenching)', COALESCE(SUM(flat_bearing_area_rejected), 0) FROM process_quenching_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Falling Gauge (Quenching)', COALESCE(SUM(falling_gauge_rejected), 0) FROM process_quenching_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Tempering Temperature', COALESCE(SUM(tempering_temperature_rejected), 0) FROM process_tempering_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Tempering Duration', COALESCE(SUM(tempering_duration_rejected), 0) FROM process_tempering_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Box Gauge (Final)', COALESCE(SUM(box_gauge_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Flat Bearing Area (Final)', COALESCE(SUM(flat_bearing_area_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Falling Gauge (Final)', COALESCE(SUM(falling_gauge_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Surface Defect', COALESCE(SUM(surface_defect_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Embossing (Final)', COALESCE(SUM(embossing_defect_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Marking', COALESCE(SUM(marking_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Tempering Hardness', COALESCE(SUM(tempering_hardness_rejected), 0) FROM process_final_check_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Toe Load', COALESCE(SUM(toe_load_rejected), 0) FROM process_testing_finishing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Weight', COALESCE(SUM(weight_rejected), 0) FROM process_testing_finishing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'Paint Identification', COALESCE(SUM(paint_identification_rejected), 0) FROM process_testing_finishing_data WHERE created_at BETWEEN :startDate AND :endDate
            UNION ALL
            SELECT 'ERC Coating', COALESCE(SUM(erc_coating_rejected), 0) FROM process_testing_finishing_data WHERE created_at BETWEEN :startDate AND :endDate
           
    ) x
    """, nativeQuery = true)
    Long getTotalDefects(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(p.temperingAccepted), 0), COALESCE(SUM(p.totalRejected), 0) FROM ProcessLineFinalResult p")
    List<Object[]> sumProcessAcceptedAndRejected();

    @Query(value = """
                SELECT
                    SUM(COALESCE(p.tempering_accepted, 0)),
                    SUM(COALESCE(p.total_rejected, 0))
                FROM inspection_calls ic
                INNER JOIN po_header ph ON ic.po_no = ph.po_no
                INNER JOIN process_line_final_result p ON p.inspection_call_no = ic.ic_number
                INNER JOIN (
                    SELECT w.REQUESTID, w.STATUS
                    FROM WORKFLOW_TRANSITION w
                    INNER JOIN (
                        SELECT REQUESTID, MAX(WORKFLOWTRANSITIONID) AS max_id
                        FROM WORKFLOW_TRANSITION
                        GROUP BY REQUESTID
                    ) latest ON w.REQUESTID = latest.REQUESTID AND w.WORKFLOWTRANSITIONID = latest.max_id
                ) wf ON wf.REQUESTID = ic.ic_number
                WHERE (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
                AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
                AND wf.STATUS = 'SEND_CALL_TO_IBS'
                AND (CASE WHEN p.date_of_inspection IS NOT NULL THEN DATE(p.date_of_inspection) ELSE DATE(p.created_at) END) BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    List<Object[]> sumProcessAcceptedAndRejectedRevisedLogic(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
            @org.springframework.data.repository.query.Param("vendorPlantCode") String vendorPlantCode,
            @org.springframework.data.repository.query.Param("zonalRailway") String zonalRailway);

    @Query(value = """
                SELECT
                    SUM(COALESCE(p.tempering_accepted, 0)),
                    SUM(COALESCE(p.total_rejected, 0))
                FROM inspection_calls ic
                INNER JOIN process_line_final_result p ON p.inspection_call_no = ic.ic_number
                INNER JOIN (
                    SELECT w.REQUESTID, w.STATUS
                    FROM WORKFLOW_TRANSITION w
                    INNER JOIN (
                        SELECT REQUESTID, MAX(WORKFLOWTRANSITIONID) AS max_id
                        FROM WORKFLOW_TRANSITION
                        GROUP BY REQUESTID
                    ) latest ON w.REQUESTID = latest.REQUESTID AND w.WORKFLOWTRANSITIONID = latest.max_id
                ) wf ON wf.REQUESTID = ic.ic_number
                WHERE wf.STATUS = 'SEND_CALL_TO_IBS'
            """, nativeQuery = true)
    List<Object[]> sumProcessAcceptedAndRejectedAllTime();

    @Query(value = """
                SELECT
                    DATE_FORMAT(IFNULL(p.date_of_inspection, p.created_at), '%b-%y') AS Month_Year,
                    SUM(COALESCE(p.total_rejected, 0)) AS Total_Rejected,
                    SUM(COALESCE(p.shearing_manufactured, p.total_manufactured, 0)) AS Total_Produced,
                    ROUND(SUM(COALESCE(p.total_rejected, 0)) * 100.0 /
                          NULLIF(SUM(COALESCE(p.shearing_manufactured, p.total_manufactured, 0)), 0), 2) AS Rejection_Percentage
                FROM process_line_final_result p
                WHERE IFNULL(p.date_of_inspection, p.created_at) BETWEEN :startDate AND :endDate
                GROUP BY
                    YEAR(IFNULL(p.date_of_inspection, p.created_at)),
                    MONTH(IFNULL(p.date_of_inspection, p.created_at)),
                    Month_Year
                ORDER BY MIN(IFNULL(p.date_of_inspection, p.created_at)) ASC
            """, nativeQuery = true)
    List<Object[]> findMonthlyRejectionTrend(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
                SELECT
                    COALESCE(i.supplier_name, 'Other/Unknown') AS supplier_name,
                    ROUND(SUM(COALESCE(p.mpi_rejected, 0)) * 100.0 /
                          NULLIF(SUM(COALESCE(p.mpi_manufactured, p.total_manufactured, 0)), 0), 2) AS mpi_rejection_percentage
                FROM process_line_final_result p
                LEFT JOIN inventory_entries i ON TRIM(p.heat_number) = TRIM(i.heat_number)
                WHERE p.created_at >= :startDate
                GROUP BY supplier_name
                ORDER BY mpi_rejection_percentage DESC
                LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findMpiRejectionBySupplier(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate);

    @Query(value = """
                SELECT
                    COALESCE(i.supplier_name, 'Other/Unknown') AS supplier_name,
                    ROUND(SUM(COALESCE(p.mpi_rejected, 0)) * 100.0 /
                          NULLIF(SUM(COALESCE(p.mpi_manufactured, p.total_manufactured, 0)), 0), 2) AS mpi_rejection_percentage
                FROM process_line_final_result p
                LEFT JOIN inventory_entries i ON TRIM(p.heat_number) = TRIM(i.heat_number)
                WHERE p.created_at BETWEEN :startDate AND :endDate
                GROUP BY supplier_name
                ORDER BY mpi_rejection_percentage DESC
                LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findMpiRejectionBySupplierBetweenDates(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
                SELECT
                    ic.company_name AS manufacture,
                    SUM(COALESCE(p.shearing_manufactured, 0)) AS totalInspected,
                    SUM(COALESCE(p.tempering_accepted, 0)) AS totalAccepted,
                    SUM(COALESCE(p.total_rejected, 0)) AS totalRejected
                FROM process_line_final_result p
                JOIN inspection_calls ic ON ic.ic_number = p.inspection_call_no
                WHERE (CASE WHEN p.date_of_inspection IS NOT NULL THEN p.date_of_inspection ELSE p.created_at END) BETWEEN :startDate AND :endDate
                GROUP BY ic.company_name
            """, countQuery = """
                SELECT COUNT(DISTINCT ic.company_name)
                FROM process_line_final_result p
                JOIN inspection_calls ic ON ic.ic_number = p.inspection_call_no
                WHERE (CASE WHEN p.date_of_inspection IS NOT NULL THEN p.date_of_inspection ELSE p.created_at END) BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    Page<Object[]> fetchMpiaReport(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate,
            Pageable pageable);

    @Query(value = """
                SELECT
                    ic.company_name AS manufacture,
                    SUM(COALESCE(p.shearing_manufactured, 0)) AS totalInspected,
                    SUM(COALESCE(p.total_rejected, 0)) AS totalRejected,
                    SUM(COALESCE(p.shearing_rejected, 0)) AS shearingRejected,
                    SUM(COALESCE(p.turning_rejected, 0)) AS turningRejected,
                    SUM(COALESCE(p.mpi_rejected, 0)) AS mpiRejected,
                    SUM(COALESCE(p.forging_rejected, 0)) AS forgingRejected,
                    SUM(COALESCE(p.quenching_rejected, 0)) AS quenchingRejected,
                    SUM(COALESCE(p.tempering_rejected, 0)) AS temperingRejected,
                    SUM(COALESCE(p.shearing_manufactured, 0)) AS shearingManufactured,
                    SUM(COALESCE(p.turning_manufactured, 0)) AS turningManufactured,
                    SUM(COALESCE(p.mpi_manufactured, 0)) AS mpiManufactured,
                    SUM(COALESCE(p.forging_manufactured, 0)) AS forgingManufactured,
                    SUM(COALESCE(p.quenching_manufactured, 0)) AS quenchingManufactured,
                    SUM(COALESCE(p.tempering_manufactured, 0)) AS temperingManufactured,
                    SUM(COALESCE(p.tempering_accepted, 0)) AS totalAccepted
                FROM process_line_final_result p
                JOIN inspection_calls ic ON ic.ic_number = p.inspection_call_no
                WHERE (:startDate IS NULL OR (CASE WHEN p.date_of_inspection IS NOT NULL THEN p.date_of_inspection ELSE p.created_at END) >= :startDate)
                  AND (:endDate IS NULL OR (CASE WHEN p.date_of_inspection IS NOT NULL THEN p.date_of_inspection ELSE p.created_at END) <= :endDate)
                GROUP BY ic.company_name
            """, nativeQuery = true)
    List<Object[]> fetchProcessInspectionQualityTable(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    List<ProcessLineFinalResult> findByDateOfInspectionBetween(LocalDate startDate, LocalDate endDate);

    @Query("""
                SELECT new com.sarthi.dto.summaryDtos.PlantShiftWiseRawDto(

                    p.dateOfInspection,
                    p.shift,
                    p.lotNumber,
                    i.poNo,
                    i.poSerialNo,
                    p.shearingManufactured,
                    p.temperingManufactured,
                    p.temperingAccepted,
                    p.totalRejected
                )

                FROM ProcessLineFinalResult p
                JOIN InspectionCall i
                    ON p.inspectionCallNo = i.icNumber

                WHERE p.dateOfInspection BETWEEN :startDate AND :endDate
                AND i.placeOfInspection = :poiCode
            """)
    List<PlantShiftWiseRawDto> getPlantShiftWiseRawData(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("poiCode") String poiCode);

    List<ProcessLineFinalResult> findByInspectionCallNoIn(List<String> callNos);

    @Query("""
            SELECT
                p.inspectionCallNo,

                SUM(COALESCE(p.shearingManufactured,0)),
                SUM(COALESCE(p.shearingRejected,0)),

                SUM(COALESCE(p.turningManufactured,0)),
                SUM(COALESCE(p.turningRejected,0)),

                SUM(COALESCE(p.mpiManufactured,0)),
                SUM(COALESCE(p.mpiRejected,0)),

                SUM(COALESCE(p.forgingManufactured,0)),
                SUM(COALESCE(p.forgingRejected,0)),

                SUM(COALESCE(p.quenchingManufactured,0)),
                SUM(COALESCE(p.quenchingRejected,0)),

                SUM(COALESCE(p.temperingManufactured,0)),
                SUM(COALESCE(p.temperingRejected,0))

            FROM ProcessLineFinalResult p
            WHERE p.inspectionCallNo IN :callNos
            GROUP BY p.inspectionCallNo
            """)
    List<Object[]> getProcessSummary(
            @Param("callNos") List<String> callNos);

    /*
     * @Query(value = """
     * 
     * SELECT
     * ph.case_no AS caseNumber,
     * 
     * DATE(ic.created_at) AS callDate,
     * 
     * ic.place_of_inspection AS placeOfInspection,
     * 
     * CAST(um.employee_code AS CHAR) AS ieEmployeeNumber,
     * 
     * 'IC Generated' AS callStatus,
     * 
     * ic.po_serial_no AS poItemSerialNumber,
     * 
     * CAST(p.book_no AS CHAR) AS bkNumber,
     * 
     * CAST(p.set_no AS CHAR) AS setNumber,
     * 
     * DATE(p.created_at) AS icDate,
     * 
     * COALESCE(SUM(DISTINCT pr.offered_qty),0)
     * AS quantityOffered,
     * 
     * COALESCE(SUM(pr.total_accepted),0)
     * AS quantityPassed,
     * 
     * COALESCE(SUM(pr.total_rejected),0)
     * AS quantityRejected,
     * ic.ic_number AS callNo
     * 
     * FROM process_ic_edit p
     * 
     * INNER JOIN inspection_calls ic
     * ON ic.ic_number =
     * SUBSTRING_INDEX(
     * SUBSTRING_INDEX(p.ic_number,'/',2),
     * '/',
     * -1
     * )
     * 
     * INNER JOIN po_header ph
     * ON ph.po_no = ic.po_no
     * 
     * INNER JOIN user_master um
     * ON um.userid = p.created_by
     * 
     * LEFT JOIN process_line_final_result pr
     * ON pr.inspection_call_no = ic.ic_number
     * 
     * LEFT JOIN ibs_call_registration icr
     * ON icr.call_number = ic.ic_number
     * 
     * WHERE icr.call_number IS NULL
     * OR icr.status = 'Failed'
     * 
     * GROUP BY
     * ph.case_no,
     * ic.created_at,
     * ic.place_of_inspection,
     * um.employee_code,
     * ic.po_serial_no,
     * p.book_no,
     * p.set_no,
     * p.created_at,ic.ic_number
     * 
     * """,
     * nativeQuery = true)
     * List<Object[]> getProcessInspectionCalls();
     */

    @Query(value = """
            SELECT
                COALESCE(ph.case_no, '')                                AS caseNumber,
                DATE(ic.created_at)                                     AS callDate,
                COALESCE(ic.place_of_inspection, '')                    AS placeOfInspection,
                COALESCE(pm.ibs_vendor_code, ic.place_of_inspection)    AS ibsManufacturedCode,
                CAST(COALESCE(um_wt.employee_code, um_p.employee_code, um_ic.employee_code, wt_latest.assigned_to_user, wt_latest.createdby, p.created_by, psc.created_by, ic.created_by) AS CHAR) AS ieEmployeeNumber,
                'A'                                                     AS callStatus,
                'P'                                                     AS typeOfCall,
                (CASE 
                    WHEN ic.po_no LIKE '%/%' AND SUBSTRING_INDEX(ic.po_no, '/', -1) <> '' THEN SUBSTRING_INDEX(ic.po_no, '/', -1)
                    WHEN ic.po_serial_no IS NOT NULL AND TRIM(ic.po_serial_no) <> '' THEN TRIM(ic.po_serial_no)
                    ELSE '1'
                END)                                                    AS poItemSerialNumber,
                CAST(COALESCE(p.book_no, psc.book_no, '') AS CHAR)      AS bkNumber,
                CAST(COALESCE(p.set_no, psc.set_no, '') AS CHAR)        AS setNumber,
                DATE(COALESCE(p.created_at, psc.created_at, icd.created_on, ic.updated_at, ic.created_at)) AS icDate,
                COALESCE(pr.offered_qty, (SELECT SUM(pid.offered_qty) FROM process_inspection_details pid WHERE pid.ic_id = ic.id), 0) AS quantityOffered,
                COALESCE(pr.total_accepted, 0)                          AS quantityPassed,
                COALESCE(pr.total_rejected, 0)                          AS quantityRejected,
                ic.ic_number                                            AS callNo,
                COALESCE(
                    NULLIF(icd.certificate_no, ''),
                    (CASE WHEN p.ic_number LIKE '%/%' THEN p.ic_number ELSE NULL END),
                    (CASE WHEN psc.ic_number LIKE '%/%' THEN psc.ic_number ELSE NULL END),
                    NULLIF(p.ic_number, ''),
                    NULLIF(psc.ic_number, ''),
                    ic.ic_number
                )                                                       AS callNumber,
                0.0                                                     AS cancelCharges,
                0.0                                                     AS rejectCharges,
                COALESCE(NULLIF(TRIM(wt_latest.rio), ''), '')           AS plantRio
            FROM inspection_calls ic
            LEFT JOIN process_ic_edit p
                   ON CONVERT(p.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(p.ic_number, '/', 2), '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(SUBSTRING_INDEX(p.ic_number, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(p.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
            LEFT JOIN process_ic_save_changes psc
                   ON CONVERT(psc.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT icd1.call_no, icd1.certificate_no, icd1.created_on
                FROM inspection_complete_details icd1
                INNER JOIN (
                    SELECT call_no, MAX(id) AS max_id
                    FROM inspection_complete_details
                    GROUP BY call_no
                ) latest_icd ON icd1.id = latest_icd.max_id
            ) icd ON CONVERT(icd.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_header ph
                   ON CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT((CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN sarthi_ibs_poi_mapping pm
                   ON CONVERT(pm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.place_of_inspection USING utf8mb4) COLLATE utf8mb4_unicode_ci
                  AND pm.product_type = 'erc'
            LEFT JOIN (
                SELECT wt1.requestid, wt1.status, wt1.action, wt1.job_status, wt1.createdby, wt1.assigned_to_user, wt1.rio
                FROM workflow_transition wt1
                INNER JOIN (
                    SELECT requestid, MAX(workflowtransitionid) AS max_wt_id
                    FROM workflow_transition
                    GROUP BY requestid
                ) latest_wt ON wt1.workflowtransitionid = latest_wt.max_wt_id
            ) wt_latest
                   ON CONVERT(wt_latest.requestid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR (p.ic_number IS NOT NULL AND CONVERT(wt_latest.requestid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(p.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci)
            LEFT JOIN user_master um_wt
                   ON CONVERT(um_wt.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(wt_latest.assigned_to_user, wt_latest.createdby) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um_wt.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(wt_latest.assigned_to_user, wt_latest.createdby) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um_p
                   ON CONVERT(um_p.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(p.created_by, psc.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um_p.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(p.created_by, psc.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um_ic
                   ON CONVERT(um_ic.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um_ic.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT 
                    pr_sub.inspection_call_no,
                    SUM(COALESCE(pr_sub.offered_qty, 0)) AS offered_qty,
                    SUM(COALESCE(pr_sub.total_accepted, 0)) AS total_accepted,
                    SUM(COALESCE(pr_sub.total_rejected, 0)) AS total_rejected
                FROM process_line_final_result pr_sub
                GROUP BY pr_sub.inspection_call_no
            ) pr ON CONVERT(pr.inspection_call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT icr1.*
                FROM ibs_call_registration icr1
                INNER JOIN (
                    SELECT call_number, MAX(version) AS max_version
                    FROM ibs_call_registration
                    GROUP BY call_number
                ) latest ON CONVERT(latest.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(icr1.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                       AND latest.max_version = icr1.version
            ) icr ON CONVERT(icr.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            WHERE (
                UPPER(COALESCE(ic.type_of_call, '')) LIKE '%PROCESS%' 
                OR UPPER(COALESCE(ic.type_of_call, '')) = 'P' 
                OR UPPER(COALESCE(ic.ic_number, '')) LIKE 'EP%'
            )
            AND (
                UPPER(COALESCE(ic.status, '')) LIKE '%SEND_CALL_TO_IBS%'
                OR UPPER(COALESCE(ic.status, '')) LIKE '%SENT_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.status, '')) LIKE '%SEND_CALL_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.action, '')) LIKE '%SEND_CALL_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.status, '')) LIKE '%SENT_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.action, '')) LIKE '%SENT_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.action, '')) LIKE '%SEND CALL TO IBS%'
            )
            AND (
                icr.call_number IS NULL
                OR UPPER(icr.status) = 'FAILED'
            )
            GROUP BY
                ph.case_no,
                ic.created_at,
                ic.place_of_inspection,
                pm.ibs_vendor_code,
                um_wt.employee_code,
                um_p.employee_code,
                um_ic.employee_code,
                wt_latest.assigned_to_user,
                wt_latest.createdby,
                p.created_by,
                psc.created_by,
                ic.created_by,
                ic.po_no,
                ic.po_serial_no,
                p.book_no,
                psc.book_no,
                p.set_no,
                psc.set_no,
                p.created_at,
                psc.created_at,
                icd.created_on,
                ic.updated_at,
                ic.ic_number,
                icd.certificate_no,
                p.ic_number,
                psc.ic_number,
                pr.offered_qty,
                pr.total_accepted,
                pr.total_rejected,
                wt_latest.rio,
                ic.id
            """, nativeQuery = true)
    List<Object[]> getProcessInspectionCalls();

    @Query(value = """
            SELECT
                COALESCE(ph.case_no, '')                                AS caseNumber,
                DATE(ic.created_at)                                     AS callDate,
                COALESCE(ic.place_of_inspection, '')                    AS placeOfInspection,
                COALESCE(pm.ibs_vendor_code, ic.place_of_inspection)    AS ibsManufacturedCode,
                CAST(COALESCE(um_wt.employee_code, um_p.employee_code, um_ic.employee_code, wt_latest.assigned_to_user, wt_latest.createdby, p.created_by, psc.created_by, ic.created_by) AS CHAR) AS ieEmployeeNumber,
                'A'                                                     AS callStatus,
                'P'                                                     AS typeOfCall,
                (CASE 
                    WHEN ic.po_no LIKE '%/%' AND SUBSTRING_INDEX(ic.po_no, '/', -1) <> '' THEN SUBSTRING_INDEX(ic.po_no, '/', -1)
                    WHEN ic.po_serial_no IS NOT NULL AND TRIM(ic.po_serial_no) <> '' THEN TRIM(ic.po_serial_no)
                    ELSE '1'
                END)                                                    AS poItemSerialNumber,
                CAST(COALESCE(p.book_no, psc.book_no, '') AS CHAR)      AS bkNumber,
                CAST(COALESCE(p.set_no, psc.set_no, '') AS CHAR)        AS setNumber,
                DATE(COALESCE(p.created_at, psc.created_at, icd.created_on, ic.updated_at, ic.created_at)) AS icDate,
                COALESCE(pr.offered_qty, (SELECT SUM(pid.offered_qty) FROM process_inspection_details pid WHERE pid.ic_id = ic.id), 0) AS quantityOffered,
                COALESCE(pr.total_accepted, 0)                          AS quantityPassed,
                COALESCE(pr.total_rejected, 0)                          AS quantityRejected,
                ic.ic_number                                            AS callNo,
                COALESCE(
                    NULLIF(icd.certificate_no, ''),
                    (CASE WHEN p.ic_number LIKE '%/%' THEN p.ic_number ELSE NULL END),
                    (CASE WHEN psc.ic_number LIKE '%/%' THEN psc.ic_number ELSE NULL END),
                    NULLIF(p.ic_number, ''),
                    NULLIF(psc.ic_number, ''),
                    ic.ic_number
                )                                                       AS callNumber,
                0.0                                                     AS cancelCharges,
                0.0                                                     AS rejectCharges,
                COALESCE(NULLIF(TRIM(wt_latest.rio), ''), '')           AS plantRio
            FROM inspection_calls ic
            LEFT JOIN process_ic_edit p
                   ON CONVERT(p.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(p.ic_number, '/', 2), '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(SUBSTRING_INDEX(p.ic_number, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(p.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
            LEFT JOIN process_ic_save_changes psc
                   ON CONVERT(psc.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT icd1.call_no, icd1.certificate_no, icd1.created_on
                FROM inspection_complete_details icd1
                INNER JOIN (
                    SELECT call_no, MAX(id) AS max_id
                    FROM inspection_complete_details
                    GROUP BY call_no
                ) latest_icd ON icd1.id = latest_icd.max_id
            ) icd ON CONVERT(icd.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_header ph
                   ON CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT((CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN sarthi_ibs_poi_mapping pm
                   ON CONVERT(pm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.place_of_inspection USING utf8mb4) COLLATE utf8mb4_unicode_ci
                  AND pm.product_type = 'erc'
            LEFT JOIN (
                SELECT wt1.requestid, wt1.assigned_to_user, wt1.createdby, wt1.status, wt1.action, wt1.rio
                FROM workflow_transition wt1
                INNER JOIN (
                    SELECT requestid, MAX(workflowtransitionid) AS max_wt_id
                    FROM workflow_transition
                    GROUP BY requestid
                ) latest_wt ON wt1.workflowtransitionid = latest_wt.max_wt_id
            ) wt_latest
                   ON CONVERT(wt_latest.requestid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR (p.ic_number IS NOT NULL AND CONVERT(wt_latest.requestid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(p.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci)
            LEFT JOIN user_master um_wt
                   ON CONVERT(um_wt.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(wt_latest.assigned_to_user, wt_latest.createdby) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um_wt.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(wt_latest.assigned_to_user, wt_latest.createdby) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um_p
                   ON CONVERT(um_p.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(p.created_by, psc.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um_p.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(p.created_by, psc.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um_ic
                   ON CONVERT(um_ic.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um_ic.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT 
                    pr_sub.inspection_call_no,
                    SUM(COALESCE(pr_sub.offered_qty, 0)) AS offered_qty,
                    SUM(COALESCE(pr_sub.total_accepted, 0)) AS total_accepted,
                    SUM(COALESCE(pr_sub.total_rejected, 0)) AS total_rejected
                FROM process_line_final_result pr_sub
                GROUP BY pr_sub.inspection_call_no
            ) pr ON CONVERT(pr.inspection_call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT icr1.*
                FROM ibs_call_registration icr1
                INNER JOIN (
                    SELECT call_number, MAX(version) AS max_version
                    FROM ibs_call_registration
                    GROUP BY call_number
                ) latest ON CONVERT(latest.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(icr1.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                       AND latest.max_version = icr1.version
            ) icr ON CONVERT(icr.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            WHERE (
                UPPER(COALESCE(ic.type_of_call, '')) LIKE '%PROCESS%' 
                OR UPPER(COALESCE(ic.type_of_call, '')) = 'P' 
                OR UPPER(COALESCE(ic.ic_number, '')) LIKE 'EP%'
            )
            AND UPPER(icr.status) = 'SUCCESS'
            GROUP BY
                ph.case_no,
                ic.created_at,
                ic.place_of_inspection,
                pm.ibs_vendor_code,
                um_wt.employee_code,
                um_p.employee_code,
                um_ic.employee_code,
                wt_latest.assigned_to_user,
                wt_latest.createdby,
                p.created_by,
                psc.created_by,
                ic.created_by,
                ic.po_no,
                ic.po_serial_no,
                p.book_no,
                psc.book_no,
                p.set_no,
                psc.set_no,
                p.created_at,
                psc.created_at,
                icd.created_on,
                ic.updated_at,
                ic.ic_number,
                icd.certificate_no,
                p.ic_number,
                psc.ic_number,
                pr.offered_qty,
                pr.total_accepted,
                pr.total_rejected,
                wt_latest.rio,
                ic.id
            """, nativeQuery = true)
    List<Object[]> getProcessCompletedInspectionCalls();

    /** Bulk fetch: SUM(tempering_accepted) per inspection_call_no for a list of Process call numbers */
    @Query(value = """
        SELECT p.inspection_call_no, COALESCE(SUM(p.tempering_accepted), 0)
        FROM process_line_final_result p
        WHERE p.inspection_call_no IN :icNumbers
        GROUP BY p.inspection_call_no
        """, nativeQuery = true)
    List<Object[]> sumAcceptedQtyByIcNumbers(@Param("icNumbers") List<String> icNumbers);

    /** Fetch SUM(tempering_accepted) for a given lotNumber and heatNumber */
    @Query(value = """
        SELECT COALESCE(SUM(p.tempering_accepted), 0)
        FROM process_line_final_result p
        WHERE TRIM(p.lot_number) = TRIM(:lotNumber)
          AND (TRIM(p.heat_number) = TRIM(:heatNo) OR :heatNo IS NULL OR :heatNo = '')
        """, nativeQuery = true)
    Integer sumTemperingAcceptedByLotNumberAndHeatNo(
            @Param("lotNumber") String lotNumber,
            @Param("heatNo") String heatNo);

    /**
     * Fetch accepted quantity (total_manufactured - total_rejected) for a lot and heat
     * strictly matching Process IC certificate logic. Single aggregated query - O(1), no N+1.
     */
    @Query(value = """
        SELECT GREATEST(0, COALESCE(SUM(p.total_manufactured), 0) - COALESCE(SUM(p.total_rejected), 0))
        FROM process_line_final_result p
        WHERE (:requestId IS NULL OR :requestId = '' 
               OR p.inspection_call_no = :requestId 
               OR p.inspection_call_no LIKE CONCAT('%', :requestId, '%')
               OR :requestId LIKE CONCAT('%', p.inspection_call_no, '%'))
          AND (REPLACE(p.lot_number, ' ', '') = REPLACE(:lotNumber, ' ', '') OR TRIM(p.lot_number) = TRIM(:lotNumber))
          AND (:heatNo IS NULL OR :heatNo = '' OR TRIM(p.heat_number) = TRIM(:heatNo))
        """, nativeQuery = true)
    Integer sumAcceptedQtyByCallNoAndLotNumberAndHeatNo(
            @Param("requestId") String requestId,
            @Param("lotNumber") String lotNumber,
            @Param("heatNo") String heatNo);
}
