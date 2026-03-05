package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessLineFinalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

}
