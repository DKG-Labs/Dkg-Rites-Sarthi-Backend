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
            rvp.plant_name AS plantName,
            COALESCE(ip.rio, 'N/A') AS rio,
            COALESCE(um.full_name, um.username, 'N/A') AS ieName,
            'FINAL' AS stage,
            SUM(COALESCE(r.offered_qty, 0)) AS inspectedQty,
            SUM(COALESCE(r.accepted_qty, 0)) AS acceptedQty,
            SUM(COALESCE(r.rejected_qty, 0)) AS rejectedQty
        FROM rail_final_inspection_lot_results r
        LEFT JOIN rail_inspection_call ic ON CONVERT(r.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN USER_MASTER um ON um.USERID = ic.created_by
        LEFT JOIN ie_profile ip ON ip.employee_code = um.EMPLOYEE_CODE
        LEFT JOIN rail_vendor_plant rvp ON CONVERT(r.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(rvp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
        WHERE (:startDate IS NULL OR r.date_of_inspection >= :startDate)
          AND (:endDate IS NULL OR r.date_of_inspection <= :endDate)
          AND (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
          AND (:zone IS NULL OR :zone = '' OR CONVERT(ph.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zone USING utf8mb4) COLLATE utf8mb4_unicode_ci)
          AND (:vendor IS NULL OR :vendor = '' OR CONVERT(rvp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vendor USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        GROUP BY rvp.plant_name, ip.rio, um.full_name, um.username
        ORDER BY rvp.plant_name ASC
    """, nativeQuery = true)
    List<Object[]> fetchFinalPerformance(
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
        @org.springframework.data.repository.query.Param("rio") String rio,
        @org.springframework.data.repository.query.Param("zone") String zone,
        @org.springframework.data.repository.query.Param("vendor") String vendor);
}
