package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessLineFinalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.shearingManufactured) FROM ProcessLineFinalResult p WHERE p.createdAt >= :date")
    Long sumShearingManufacturedLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);
}
