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

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"batchResults"})
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
        ) latest ON CONVERT(TRIM(sfr.call_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(latest.request_id) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        INNER JOIN sleeper_workflow_transaction swt ON latest.request_id = swt.request_id AND latest.max_id = swt.workflow_transition_id
        LEFT JOIN sleeper_inspection_call sic ON CONVERT(TRIM(sfr.call_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(sic.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON (CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(sic.po_no, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND (
            CONVERT(pi.item_sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci 
            OR CONVERT(pi.item_sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(sic.sr_no, '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        )
        WHERE swt.workflow_id = 2
          AND UPPER(COALESCE(swt.status, '')) = 'SEND_CALL_TO_IBS'
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
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) NOT IN ('set', 'sets')
                THEN sfr.total_accepted ELSE 0 END), 0) AS final_accepted_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) IN ('set', 'sets')
                THEN COALESCE(sfr.accepted_sets_quantity, sfr.total_accepted) ELSE 0 END), 0) AS final_accepted_set,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) NOT IN ('set', 'sets')
                THEN sfr.total_rejected ELSE 0 END), 0) AS final_rejected_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) IN ('set', 'sets')
                THEN COALESCE(sfr.rejected_sets_quantity, sfr.total_rejected) ELSE 0 END), 0) AS final_rejected_set,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) NOT IN ('set', 'sets')
                THEN sfr.total_offered_quantity ELSE 0 END), 0) AS total_offered_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) IN ('set', 'sets')
                THEN COALESCE(sfr.offered_sets_quantity, sfr.total_offered_quantity) ELSE 0 END), 0) AS total_offered_set
        FROM sleeper_final_result sfr
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON CONVERT(TRIM(sfr.call_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(latest.request_id) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        INNER JOIN sleeper_workflow_transaction swt ON latest.request_id = swt.request_id AND latest.max_id = swt.workflow_transition_id
        LEFT JOIN sleeper_inspection_call sic ON CONVERT(TRIM(sfr.call_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(sic.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON (CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(sic.po_no, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN vendor_plant vp ON (CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(sic.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND (
            CONVERT(pi.item_sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci 
            OR CONVERT(pi.item_sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(sic.sr_no, '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        )
        WHERE swt.workflow_id = 2
          AND (UPPER(COALESCE(swt.status, '')) = 'SEND_CALL_TO_IBS' OR UPPER(COALESCE(swt.action, '')) = 'SEND_CALL_TO_IBS')
    """, nativeQuery = true)
    java.util.List<Object[]> getAllSleeperFinalSummary();

    @Query(value = """
        SELECT 
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) NOT IN ('set', 'sets')
                THEN sfr.total_accepted ELSE 0 END), 0) AS final_accepted_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) IN ('set', 'sets')
                THEN COALESCE(sfr.accepted_sets_quantity, sfr.total_accepted) ELSE 0 END), 0) AS final_accepted_set,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) NOT IN ('set', 'sets')
                THEN sfr.total_rejected ELSE 0 END), 0) AS final_rejected_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) IN ('set', 'sets')
                THEN COALESCE(sfr.rejected_sets_quantity, sfr.total_rejected) ELSE 0 END), 0) AS final_rejected_set,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) NOT IN ('set', 'sets')
                THEN sfr.total_offered_quantity ELSE 0 END), 0) AS total_offered_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) IN ('set', 'sets')
                THEN COALESCE(sfr.offered_sets_quantity, sfr.total_offered_quantity) ELSE 0 END), 0) AS total_offered_set
        FROM sleeper_final_result sfr
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON CONVERT(TRIM(sfr.call_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(latest.request_id) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        INNER JOIN sleeper_workflow_transaction swt ON latest.request_id = swt.request_id AND latest.max_id = swt.workflow_transition_id
        LEFT JOIN sleeper_inspection_call sic ON CONVERT(TRIM(sfr.call_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(TRIM(sic.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON (CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(sic.po_no, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN vendor_plant vp ON (CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
            OR CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(sic.plant_id, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND (
            CONVERT(pi.item_sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci 
            OR CONVERT(pi.item_sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(sic.sr_no, '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        )
        WHERE swt.workflow_id = 2
          AND (UPPER(COALESCE(swt.status, '')) = 'SEND_CALL_TO_IBS' OR UPPER(COALESCE(swt.action, '')) = 'SEND_CALL_TO_IBS')
          AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR :vendorPlantCode = 'all' OR
               CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(REPLACE(COALESCE(sic.plant_id, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(:vendorPlantCode, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(sfr.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(REPLACE(COALESCE(sfr.plant_id, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(:vendorPlantCode, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(swt.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(REPLACE(COALESCE(swt.plant_id, ''), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(:vendorPlantCode, ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(vp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               CONVERT(ph.vendor_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR
               CONVERT(ph.firm_details USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%') OR
               CONVERT(ph.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
               REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.vendor_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%') OR
               REPLACE(REPLACE(REPLACE(CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.vendor_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%') OR
               REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.firm_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%') OR
               REPLACE(REPLACE(REPLACE(CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(ph.firm_details, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%') OR
               REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(vp.company_name, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%') OR
               REPLACE(REPLACE(REPLACE(CONVERT(:vendorPlantCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', '') LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(CONVERT(COALESCE(vp.company_name, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci, ' ', ''), '.', ''), '+', ''), '%'))
          AND (:zonalRailway IS NULL OR :zonalRailway = '' OR :zonalRailway = 'all' OR 
               UPPER(TRIM(CONVERT(COALESCE(ph.rly_short_name, ph.rly_cd, vp.zonal_railway, '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)) = UPPER(TRIM(CONVERT(:zonalRailway USING utf8mb4) COLLATE utf8mb4_unicode_ci)))
          AND (:startDate IS NULL OR :startDate = '' OR :endDate IS NULL OR :endDate = '' OR 
               DATE(COALESCE(sfr.date_of_inspection, swt.created_date, sfr.created_at)) BETWEEN :startDate AND :endDate)
    """, nativeQuery = true)
    java.util.List<Object[]> getSleeperFinalSummaryFiltered(
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    @Query(value = """
        SELECT 
            DATE_FORMAT(IFNULL(sfr.date_of_inspection, sfr.created_at), '%b-%y') AS Month_Year,
            YEAR(IFNULL(sfr.date_of_inspection, sfr.created_at)) AS Y,
            MONTH(IFNULL(sfr.date_of_inspection, sfr.created_at)) AS M,
            SUM(COALESCE(sfr.total_rejected, 0)) AS Total_Rejected,
            SUM(COALESCE(sfr.total_accepted, 0)) AS Total_Accepted,
            SUM(COALESCE(sfr.total_offered_quantity, 0)) AS Total_Offered
        FROM sleeper_final_result sfr
        WHERE IFNULL(sfr.date_of_inspection, sfr.created_at) BETWEEN :startDate AND :endDate
        GROUP BY Y, M, Month_Year
        ORDER BY Y ASC, M ASC
    """, nativeQuery = true)
    java.util.List<Object[]> findMonthlyFinalRejections(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}
