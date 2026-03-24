package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessLineFinalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
     * Get the sum of accepted quantities across all stages for a specific call and lot.
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


    @Query(value = """
            SELECT 
            COALESCE(SUM(pt.tempering_temperature_rejected),0) +
            COALESCE(SUM(pt.tempering_duration_rejected),0) +

            COALESCE(SUM(pf.box_gauge_rejected),0) +
            COALESCE(SUM(pf.flat_bearing_area_rejected),0) +
            COALESCE(SUM(pf.falling_gauge_rejected),0) +
            COALESCE(SUM(pf.surface_defect_rejected),0) +
            COALESCE(SUM(pf.embossing_defect_rejected),0) +
            COALESCE(SUM(pf.marking_rejected),0) +
            COALESCE(SUM(pf.tempering_hardness_rejected),0) +

            COALESCE(SUM(ptf.toe_load_rejected),0) +
            COALESCE(SUM(ptf.weight_rejected),0) +
            COALESCE(SUM(ptf.paint_identification_rejected),0) +
            COALESCE(SUM(ptf.erc_coating_rejected),0)

            FROM process_line_final_result pl

            LEFT JOIN process_tempering_data pt
            ON pt.inspection_call_no = pl.inspection_call_no
            AND pt.lot_no = pl.lot_number
            AND pt.shift = pl.shift
            AND DATE(pt.created_at) = DATE(pl.created_at)

            LEFT JOIN process_final_check_data pf
            ON pf.inspection_call_no = pl.inspection_call_no
            AND pf.lot_no = pl.lot_number
            AND pf.shift = pl.shift
            AND DATE(pf.created_at) = DATE(pl.created_at)

            LEFT JOIN process_testing_finishing_data ptf
            ON ptf.inspection_call_no = pl.inspection_call_no
            AND ptf.lot_no = pl.lot_number
            AND ptf.shift = pl.shift
            AND DATE(ptf.created_at) = DATE(pl.created_at)

            WHERE pl.inspection_call_no = :callId
            AND pl.lot_number = :lotNumber
            AND pl.shift = :shift
            AND pl.created_at BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    Integer getTotalTemperingRejected(
            String callId,
            String lotNumber,
            String shift,
            LocalDateTime startDate,
            LocalDateTime endDate
    );


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
    List<Object[]> findTop5ProcessPerformanceNewLogic(@org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

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
        LIMIT 5
    """, nativeQuery = true)
    List<Object[]> findTop5ProcessPerformanceRevisedLogic(@org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

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
    List<Object[]> findWorst5ProcessPerformanceNewLogic(@org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

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
        LIMIT 5
    """, nativeQuery = true)
    List<Object[]> findWorst5ProcessPerformanceRevisedLogic(@org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

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
    List<Object[]> sumStepWiseRejectionLast30Days(@org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.temperingManufactured) FROM ProcessLineFinalResult p WHERE p.createdAt >= :date")
    Long sumTemperingManufacturedLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    // ===== NEW: Pareto Analysis – aggregate rejections across all process tables =====
    @org.springframework.data.jpa.repository.Query(value = """
        SELECT 'Forging Temp' AS param_name, COALESCE(SUM(forging_temp_rejected), 0) AS total FROM process_forging_data
        UNION ALL
        SELECT 'Forging Stabilisation', COALESCE(SUM(forging_stabilisation_rejection_rejected), 0) FROM process_forging_data
        UNION ALL
        SELECT 'Improper Forging', COALESCE(SUM(improper_forging_rejected), 0) FROM process_forging_data
        UNION ALL
        SELECT 'Forging Defect', COALESCE(SUM(forging_defect_rejected), 0) FROM process_forging_data
        UNION ALL
        SELECT 'Embossing Defect', COALESCE(SUM(embossing_defect_rejected), 0) FROM process_forging_data
        UNION ALL
        SELECT 'MPI', COALESCE(SUM(mpi_rejected), 0) FROM process_mpi_data
        UNION ALL
        SELECT 'Length Cut Bar', COALESCE(SUM(length_cut_bar_rejected), 0) FROM process_shearing_data
        UNION ALL
        SELECT 'Improper Dia', COALESCE(SUM(improper_dia_rejected), 0) FROM process_shearing_data
        UNION ALL
        SELECT 'Sharp Edges', COALESCE(SUM(sharp_edges_rejected), 0) FROM process_shearing_data
        UNION ALL
        SELECT 'Cracked Edges', COALESCE(SUM(cracked_edges_rejected), 0) FROM process_shearing_data
        UNION ALL
        SELECT 'Parallel Length', COALESCE(SUM(parallel_length_rejected), 0) FROM process_turning_data
        UNION ALL
        SELECT 'Full Turning Length', COALESCE(SUM(full_turning_length_rejected), 0) FROM process_turning_data
        UNION ALL
        SELECT 'Turning Dia', COALESCE(SUM(turning_dia_rejected), 0) FROM process_turning_data
        UNION ALL
        SELECT 'Quenching Hardness', COALESCE(SUM(quenching_hardness_rejected), 0) FROM process_quenching_data
        UNION ALL
        SELECT 'Box Gauge (Quenching)', COALESCE(SUM(box_gauge_rejected), 0) FROM process_quenching_data
        UNION ALL
        SELECT 'Flat Bearing Area (Quenching)', COALESCE(SUM(flat_bearing_area_rejected), 0) FROM process_quenching_data
        UNION ALL
        SELECT 'Falling Gauge (Quenching)', COALESCE(SUM(falling_gauge_rejected), 0) FROM process_quenching_data
        UNION ALL
        SELECT 'Tempering Temperature', COALESCE(SUM(tempering_temperature_rejected), 0) FROM process_tempering_data
        UNION ALL
        SELECT 'Tempering Duration', COALESCE(SUM(tempering_duration_rejected), 0) FROM process_tempering_data
        UNION ALL
        SELECT 'Box Gauge (Final)', COALESCE(SUM(box_gauge_rejected), 0) FROM process_final_check_data
        UNION ALL
        SELECT 'Flat Bearing Area (Final)', COALESCE(SUM(flat_bearing_area_rejected), 0) FROM process_final_check_data
        UNION ALL
        SELECT 'Falling Gauge (Final)', COALESCE(SUM(falling_gauge_rejected), 0) FROM process_final_check_data
        UNION ALL
        SELECT 'Surface Defect', COALESCE(SUM(surface_defect_rejected), 0) FROM process_final_check_data
        UNION ALL
        SELECT 'Embossing (Final)', COALESCE(SUM(embossing_defect_rejected), 0) FROM process_final_check_data
        UNION ALL
        SELECT 'Marking', COALESCE(SUM(marking_rejected), 0) FROM process_final_check_data
        UNION ALL
        SELECT 'Tempering Hardness', COALESCE(SUM(tempering_hardness_rejected), 0) FROM process_final_check_data
        UNION ALL
        SELECT 'Toe Load', COALESCE(SUM(toe_load_rejected), 0) FROM process_testing_finishing_data
        UNION ALL
        SELECT 'Weight', COALESCE(SUM(weight_rejected), 0) FROM process_testing_finishing_data
        UNION ALL
        SELECT 'Paint Identification', COALESCE(SUM(paint_identification_rejected), 0) FROM process_testing_finishing_data
        UNION ALL
        SELECT 'ERC Coating', COALESCE(SUM(erc_coating_rejected), 0) FROM process_testing_finishing_data
        ORDER BY total DESC
        LIMIT 10
        """, nativeQuery = true)
    java.util.List<Object[]> getParetoAnalysisRejections();

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(p.temperingAccepted), 0), COALESCE(SUM(p.totalRejected), 0) FROM ProcessLineFinalResult p")
    List<Object[]> sumProcessAcceptedAndRejected();

    @Query(value = """
        SELECT 
            SUM(COALESCE(p.tempering_accepted, 0)), 
            SUM(COALESCE(p.total_rejected, 0)) 
        FROM process_line_final_result p 
        WHERE (CASE WHEN p.date_of_inspection IS NOT NULL THEN DATE(p.date_of_inspection) ELSE DATE(p.created_at) END) BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    List<Object[]> sumProcessAcceptedAndRejectedRevisedLogic(
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate, 
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);
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
}
