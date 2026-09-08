package com.sarthi.Sleeper.repository.FInalCallRepo;

import com.sarthi.Sleeper.entity.FInalCall.SleeperFinalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface SleeperFinalResultRepository extends JpaRepository<SleeperFinalResult, Long> {

    Optional<SleeperFinalResult> findByCallNumber(String callNumber);

    @Query("""
        SELECT COALESCE(SUM(s.totalOfferedQuantity), 0)
        FROM SleeperFinalResult s
        WHERE s.poNo = :poNo 
          AND s.srNo = :srNo
          AND (:callNumber IS NULL OR s.callNumber <> :callNumber)
    """)
    BigDecimal getCumulativeOfferedQty(
        @Param("poNo") String poNo,
        @Param("srNo") String srNo,
        @Param("callNumber") String callNumber
    );

    @Query("""
        SELECT COALESCE(SUM(s.totalAccepted), 0)
        FROM SleeperFinalResult s
        WHERE s.poNo = :poNo 
          AND s.srNo = :srNo
          AND (:callNumber IS NULL OR s.callNumber <> :callNumber)
    """)
    BigDecimal getCumulativePassedQty(
        @Param("poNo") String poNo,
        @Param("srNo") String srNo,
        @Param("callNumber") String callNumber
    );

    @Query("""
        SELECT COALESCE(SUM(s.totalRejected), 0)
        FROM SleeperFinalResult s
        WHERE s.poNo = :poNo 
          AND s.srNo = :srNo
          AND (:callNumber IS NULL OR s.callNumber <> :callNumber)
    """)
    BigDecimal getCumulativeRejectedQty(
        @Param("poNo") String poNo,
        @Param("srNo") String srNo,
        @Param("callNumber") String callNumber
    );

    @Query(value = """
        SELECT 
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) != 'set' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%turnout%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%set%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%pnc%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-9790%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-4218%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-4865%' 
                THEN sfr.total_accepted ELSE 0 END), 0) AS final_accepted_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) = 'set' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%turnout%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%set%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%pnc%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-9790%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-4218%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-4865%' 
                THEN sfr.total_accepted ELSE 0 END), 0) AS final_accepted_set,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) != 'set' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%turnout%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%set%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%pnc%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-9790%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-4218%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-4865%' 
                THEN sfr.total_rejected ELSE 0 END), 0) AS final_rejected_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) = 'set' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%turnout%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%set%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%pnc%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-9790%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-4218%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-4865%' 
                THEN sfr.total_rejected ELSE 0 END), 0) AS final_rejected_set
        FROM sleeper_final_result sfr
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON TRIM(sfr.call_number) COLLATE utf8mb4_unicode_ci = TRIM(latest.request_id) COLLATE utf8mb4_unicode_ci
        INNER JOIN sleeper_workflow_transaction swt ON latest.request_id = swt.request_id AND latest.max_id = swt.workflow_transition_id
        LEFT JOIN sleeper_inspection_call sic ON TRIM(sfr.call_number) COLLATE utf8mb4_unicode_ci = TRIM(sic.call_no) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON (ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
            OR ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND (
            pi.item_sr_no COLLATE utf8mb4_unicode_ci = sic.sr_no COLLATE utf8mb4_unicode_ci 
            OR pi.item_sr_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.sr_no, '/', -1) COLLATE utf8mb4_unicode_ci
        )
        WHERE swt.workflow_id = 2
          AND (
            UPPER(COALESCE(swt.job_status, '')) IN ('IC_GENERATION', 'GENERATED', 'DSC_SIGN_IC', 'IC_SIGNED', 'COMPLETED', 'IC_ISSUE')
            OR UPPER(COALESCE(swt.action, '')) IN ('IC_GENERATION', 'DSC_SIGN_IC', 'GENERATE_IC', 'FINISH', 'COMPLETED', 'IC_ISSUE')
            OR UPPER(COALESCE(swt.status, '')) IN ('COMPLETED')
        )
        AND (
            sic.plant_id IN (:plantIds) 
            OR REPLACE(COALESCE(sic.plant_id, ''), ':', '') IN (:plantIds) 
            OR sfr.plant_id IN (:plantIds) 
            OR REPLACE(COALESCE(sfr.plant_id, ''), ':', '') IN (:plantIds) 
            OR swt.plant_id IN (:plantIds)
            OR REPLACE(COALESCE(swt.plant_id, ''), ':', '') IN (:plantIds)
        )
    """, nativeQuery = true)
    java.util.List<Object[]> getSleeperFinalSummaryByPlantIds(@Param("plantIds") java.util.Collection<String> plantIds);

    @Query(value = """
        SELECT 
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) != 'set' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%turnout%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%set%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%pnc%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-9790%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-4218%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-4865%' 
                THEN sfr.total_accepted ELSE 0 END), 0) AS final_accepted_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) = 'set' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%turnout%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%set%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%pnc%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-9790%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-4218%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-4865%' 
                THEN sfr.total_accepted ELSE 0 END), 0) AS final_accepted_set,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) != 'set' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%turnout%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%set%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%pnc%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-9790%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-4218%' 
                AND LOWER(COALESCE(sfr.sleeper_type, '')) NOT LIKE '%rt-4865%' 
                THEN sfr.total_rejected ELSE 0 END), 0) AS final_rejected_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) = 'set' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%turnout%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%set%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%pnc%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-9790%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-4218%' 
                OR LOWER(COALESCE(sfr.sleeper_type, '')) LIKE '%rt-4865%' 
                THEN sfr.total_rejected ELSE 0 END), 0) AS final_rejected_set
        FROM sleeper_final_result sfr
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON TRIM(sfr.call_number) COLLATE utf8mb4_unicode_ci = TRIM(latest.request_id) COLLATE utf8mb4_unicode_ci
        INNER JOIN sleeper_workflow_transaction swt ON latest.request_id = swt.request_id AND latest.max_id = swt.workflow_transition_id
        LEFT JOIN sleeper_inspection_call sic ON TRIM(sfr.call_number) COLLATE utf8mb4_unicode_ci = TRIM(sic.call_no) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON (ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
            OR ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND (
            pi.item_sr_no COLLATE utf8mb4_unicode_ci = sic.sr_no COLLATE utf8mb4_unicode_ci 
            OR pi.item_sr_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.sr_no, '/', -1) COLLATE utf8mb4_unicode_ci
        )
        WHERE swt.workflow_id = 2
          AND (
            UPPER(COALESCE(swt.job_status, '')) IN ('IC_GENERATION', 'GENERATED', 'DSC_SIGN_IC', 'IC_SIGNED', 'COMPLETED', 'IC_ISSUE')
            OR UPPER(COALESCE(swt.action, '')) IN ('IC_GENERATION', 'DSC_SIGN_IC', 'GENERATE_IC', 'FINISH', 'COMPLETED', 'IC_ISSUE')
            OR UPPER(COALESCE(swt.status, '')) IN ('COMPLETED')
        )
    """, nativeQuery = true)
    java.util.List<Object[]> getAllSleeperFinalSummary();
}
