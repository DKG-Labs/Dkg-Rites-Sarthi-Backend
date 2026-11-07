package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalInspectionLotResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalInspectionLotResultsRepository extends JpaRepository<RailFinalInspectionLotResults, Long> {
    Optional<RailFinalInspectionLotResults> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalInspectionLotResults> findAllByCallNo(String callNo);
    List<RailFinalInspectionLotResults> findAllByCallNoIn(List<String> callNos);
    Optional<RailFinalInspectionLotResults> findFirstByCallNo(String callNo);
    Optional<RailFinalInspectionLotResults> findFirstByCallNoOrderByIdAsc(String callNo);
    List<RailFinalInspectionLotResults> findAllByPlantIdAndShiftAndDateOfInspection(String plantId, String shift, LocalDate dateOfInspection);


    @Query(value = """
        SELECT pi.uom, SUM(COALESCE(r.accepted_qty, 0)), SUM(COALESCE(r.rejected_qty, 0))
        FROM rail_final_inspection_lot_results r
        JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON ph.po_no COLLATE utf8mb4_unicode_ci = (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND pi.item_sr_no COLLATE utf8mb4_unicode_ci = (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE ic.po_sr END) COLLATE utf8mb4_unicode_ci
        GROUP BY pi.uom
    """, nativeQuery = true)
    List<Object[]> findAcceptedAndRejectedQtyByUom();

    @Query(value = """
        SELECT 
            DATE_FORMAT(r.date_of_inspection, '%b-%y') AS Month_Year,
            YEAR(r.date_of_inspection) AS Y,
            MONTH(r.date_of_inspection) AS M,
            SUM(COALESCE(r.rejected_qty, 0)) AS Total_Final_Rejected
        FROM rail_final_inspection_lot_results r
        WHERE r.date_of_inspection BETWEEN DATE(:startDate) AND DATE(:endDate)
        GROUP BY Y, M, Month_Year
        ORDER BY Y ASC, M ASC
    """, nativeQuery = true)
    List<Object[]> findMonthlyFinalRejections(
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, 
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
        SELECT 
            CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END AS poNo,
            SUM(COALESCE(r.accepted_qty, 0)) AS totalAccepted
        FROM rail_final_inspection_lot_results r
        JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
        GROUP BY 
            CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END
    """, nativeQuery = true)
    List<Object[]> findAcceptedQtyByPo();

    @Query(value = """
        SELECT DISTINCT r.railpad_type
        FROM rail_final_inspection_lot_results r
        JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
        WHERE 
            (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci = :poNo COLLATE utf8mb4_unicode_ci
            AND (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE ic.po_sr END) COLLATE utf8mb4_unicode_ci = :poSr COLLATE utf8mb4_unicode_ci
            AND r.railpad_type IS NOT NULL AND r.railpad_type <> ''
    """, nativeQuery = true)
    List<String> findDistinctRailpadTypesByPoAndSr(@org.springframework.data.repository.query.Param("poNo") String poNo, @org.springframework.data.repository.query.Param("poSr") String poSr);

    @Query(value = """
        SELECT SUM(COALESCE(r.accepted_qty, 0))
        FROM rail_final_inspection_lot_results r
        JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
        WHERE 
            (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci = :poNo COLLATE utf8mb4_unicode_ci
            AND (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE ic.po_sr END) COLLATE utf8mb4_unicode_ci = :poSr COLLATE utf8mb4_unicode_ci
    """, nativeQuery = true)
    Long sumAcceptedQtyByPoAndSr(@org.springframework.data.repository.query.Param("poNo") String poNo, @org.springframework.data.repository.query.Param("poSr") String poSr);

    @Query(value = """
        SELECT 
            COALESCE(SUM(r.offered_qty), 0) AS totalOffered,
            COALESCE(SUM(r.rejected_qty), 0) AS totalRejected
        FROM rail_final_inspection_lot_results r
        JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
        WHERE 
            (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci = :poNo COLLATE utf8mb4_unicode_ci
            AND (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE ic.po_sr END) COLLATE utf8mb4_unicode_ci = :poSr COLLATE utf8mb4_unicode_ci
    """, nativeQuery = true)
    List<Object[]> findFinalRejectionSumsByPoAndSr(@org.springframework.data.repository.query.Param("poNo") String poNo, @org.springframework.data.repository.query.Param("poSr") String poSr);

    @Query(value = """
        SELECT 
            r.call_no AS callNo,
            SUM(COALESCE(r.offered_qty, 0)) AS offeredQty,
            SUM(COALESCE(r.accepted_qty, 0)) AS acceptedQty,
            SUM(COALESCE(r.rejected_qty, 0)) AS rejectedQty
        FROM rail_final_inspection_lot_results r
        JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
        WHERE 
            (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci = :poNo COLLATE utf8mb4_unicode_ci
            AND (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE ic.po_sr END) COLLATE utf8mb4_unicode_ci = :poSr COLLATE utf8mb4_unicode_ci
        GROUP BY r.call_no
    """, nativeQuery = true)
    List<Object[]> findCallsDetailByPoAndSr(@org.springframework.data.repository.query.Param("poNo") String poNo, @org.springframework.data.repository.query.Param("poSr") String poSr);

    @Query(value = """
        SELECT 
            CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END AS poNo,
            GROUP_CONCAT(DISTINCT r.railpad_type SEPARATOR ', ') AS railpadTypes
        FROM rail_final_inspection_lot_results r
        JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
        WHERE r.railpad_type IS NOT NULL AND r.railpad_type <> ''
        GROUP BY 
            CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END
    """, nativeQuery = true)
    List<Object[]> findDistinctRailpadTypesGroupByPo();

    @Query(value = """
        SELECT
            COALESCE(r.vendor_name, 'N/A') AS plantName,
            COALESCE(um.Region, 'N/A') AS rio,
            COALESCE(um.full_name, um.username, 'N/A') AS ieName,
            'FINAL' AS stage,
            SUM(COALESCE(r.offered_qty, 0)) AS inspectedQty,
            SUM(COALESCE(r.accepted_qty, 0)) AS acceptedQty,
            SUM(COALESCE(r.rejected_qty, 0)) AS rejectedQty
        FROM rail_final_inspection_lot_results r
        LEFT JOIN USER_MASTER um ON um.USERID = r.created_by
        LEFT JOIN rail_inspection_call ic ON CONVERT(r.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
        WHERE (:startDate IS NULL OR r.date_of_inspection >= :startDate)
          AND (:endDate IS NULL OR r.date_of_inspection <= :endDate)
          AND (:rio IS NULL OR :rio = '' OR UPPER(um.Region) = UPPER(:rio))
          AND (:zone IS NULL OR :zone = '' OR CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci)
          AND (:vendor IS NULL OR :vendor = '' OR CONVERT(r.vendor_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendor USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        GROUP BY r.vendor_name, um.Region, um.full_name, um.username
        ORDER BY r.vendor_name ASC
    """, nativeQuery = true)
    List<Object[]> fetchFinalPerformance(
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
        @org.springframework.data.repository.query.Param("rio") String rio,
        @org.springframework.data.repository.query.Param("zone") String zone,
        @org.springframework.data.repository.query.Param("vendor") String vendor);

    @Query(value = """
        SELECT 
            COALESCE(SUM(call_res.accepted_qty), 0) AS totalAccepted,
            COALESCE(SUM(call_res.rejected_qty), 0) AS totalRejected
        FROM (
            SELECT 
                r.call_no,
                CASE 
                    WHEN UPPER(MAX(COALESCE(r.railpad_type, ''))) LIKE '%NCR%' OR UPPER(MAX(COALESCE(r.railpad_type, ''))) LIKE '%NYLON%' THEN MAX(COALESCE(r.accepted_qty, 0))
                    ELSE SUM(COALESCE(r.accepted_qty, 0))
                END AS accepted_qty,
                CASE 
                    WHEN UPPER(MAX(COALESCE(r.railpad_type, ''))) LIKE '%NCR%' OR UPPER(MAX(COALESCE(r.railpad_type, ''))) LIKE '%NYLON%' THEN MAX(COALESCE(r.rejected_qty, 0))
                    ELSE SUM(COALESCE(r.rejected_qty, 0))
                END AS rejected_qty
            FROM rail_final_inspection_lot_results r
            JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
            WHERE 
                (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) = :poNo
                AND (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE ic.po_sr END) = :poSr
                AND ic.call_type = 'FINAL'
                AND ic.created_at < :createdAt
            GROUP BY r.call_no
        ) call_res
    """, nativeQuery = true)
    List<Object[]> sumQtyByPoAndSrBeforeDate(@org.springframework.data.repository.query.Param("poNo") String poNo, @org.springframework.data.repository.query.Param("poSr") String poSr, @org.springframework.data.repository.query.Param("createdAt") java.time.LocalDateTime createdAt);

    /**
     * Final (acceptance) test rejections per PO and test, all lots of the PO's calls combined.
     * Only calls with a final lot result dated within the range are counted.
     * Columns: [0] poNo, [1] testKey, [2] notOkCount
     */
    @Query(value = """
        SELECT c.po_no, t.test_key, COALESCE(SUM(t.cnt), 0) AS notOk
        FROM (
            SELECT DISTINCT r.call_no,
                   CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END AS po_no
            FROM rail_final_inspection_lot_results r
            JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
            WHERE r.date_of_inspection BETWEEN :startDate AND :endDate
        ) c
        JOIN (
            SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS call_no,
                   CONVERT('hardness' USING utf8mb4) COLLATE utf8mb4_unicode_ci AS test_key,
                   not_ok_count AS cnt FROM rail_final_hardness_test
            UNION ALL SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci, CONVERT('specificGravity' USING utf8mb4) COLLATE utf8mb4_unicode_ci, not_ok_count FROM rail_final_specific_gravity
            UNION ALL SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci, CONVERT('rubberContent' USING utf8mb4) COLLATE utf8mb4_unicode_ci, not_ok_count FROM rail_final_periodic_tga
            UNION ALL SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci, CONVERT('ashContent' USING utf8mb4) COLLATE utf8mb4_unicode_ci, not_ok_count FROM rail_final_ash_content
            UNION ALL SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci, CONVERT('reboundResilience' USING utf8mb4) COLLATE utf8mb4_unicode_ci, not_ok_count FROM rail_final_resilience_test
            UNION ALL SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci, CONVERT('dimension' USING utf8mb4) COLLATE utf8mb4_unicode_ci, dimensional_not_ok FROM rail_final_visual_dimensional_inspection
            UNION ALL SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci, CONVERT('visualTest' USING utf8mb4) COLLATE utf8mb4_unicode_ci, visual_not_ok FROM rail_final_visual_dimensional_inspection
            UNION ALL SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci, CONVERT('weight' USING utf8mb4) COLLATE utf8mb4_unicode_ci, total_not_ok FROM rail_final_weight_test
            UNION ALL SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci, CONVERT('compressionSet' USING utf8mb4) COLLATE utf8mb4_unicode_ci, not_ok_count FROM rail_final_compression_set
        ) t ON t.call_no = CONVERT(c.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
        WHERE COALESCE(t.cnt, 0) > 0
        GROUP BY c.po_no, t.test_key
    """, nativeQuery = true)
    List<Object[]> findFinalTestRejectionsGroupedByPo(
        @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") LocalDate endDate);

    /**
     * Latest final IC date per PO = MAX(railpad_final_ic_edit.created_at) of the PO's calls.
     * IC → call match follows RailpadFinalIcEditRepository (ic_number = call_no, or its 1st/2nd '/' segment).
     * Columns: [0] poNo, [1] lastIcDate (DATE)
     */
    @Query(value = """
        SELECT
            CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END AS po_no,
            DATE(MAX(f.created_at)) AS lastIcDate
        FROM railpad_final_ic_edit f
        JOIN rail_inspection_call ic
          ON CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(f.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
          OR CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(f.ic_number, '/', 2), '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
          OR CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(SUBSTRING_INDEX(f.ic_number, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        WHERE DATE(f.created_at) BETWEEN :startDate AND :endDate
        GROUP BY CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END
    """, nativeQuery = true)
    List<Object[]> findLatestFinalIcDateGroupedByPo(
        @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") LocalDate endDate);

    @Query(value = """
        SELECT
            call_res.po_no,
            COALESCE(SUM(CASE WHEN call_res.is_set = 0 THEN call_res.accepted_qty ELSE 0 END), 0) AS icNos,
            COALESCE(SUM(CASE WHEN call_res.is_set = 1 THEN call_res.accepted_qty ELSE 0 END), 0) AS icSets,
            MAX(call_res.last_date) AS lastDate
        FROM (
            SELECT
                r.call_no,
                MAX(CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) AS po_no,
                CASE WHEN UPPER(MAX(COALESCE(pi.uom, ''))) LIKE 'SET%' THEN 1 ELSE 0 END AS is_set,
                CASE WHEN UPPER(MAX(COALESCE(pi.uom, ''))) LIKE 'SET%'
                     THEN MAX(COALESCE(r.accepted_qty, 0))
                     ELSE SUM(COALESCE(r.accepted_qty, 0))
                END AS accepted_qty,
                MAX(r.date_of_inspection) AS last_date
            FROM rail_final_inspection_lot_results r
            JOIN rail_inspection_call ic ON r.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_header ph ON ph.po_no COLLATE utf8mb4_unicode_ci = (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND pi.item_sr_no COLLATE utf8mb4_unicode_ci = (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE ic.po_sr END) COLLATE utf8mb4_unicode_ci
            WHERE r.date_of_inspection BETWEEN :startDate AND :endDate
            GROUP BY r.call_no
        ) call_res
        GROUP BY call_res.po_no
    """, nativeQuery = true)
    List<Object[]> findFinalIcQtyByUomGroupedByPo(
        @org.springframework.data.repository.query.Param("startDate") LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") LocalDate endDate);
}
