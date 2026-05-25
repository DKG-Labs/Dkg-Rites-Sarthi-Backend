package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailIEProductionVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

@Repository
public interface RailIEProductionVerificationRepository extends JpaRepository<RailIEProductionVerification, Long> {
    Optional<RailIEProductionVerification> findTopByRequestIdOrderByIdDesc(Long requestId);
    java.util.List<RailIEProductionVerification> findAllByProductionUnit(String productionUnit);

    @Query(value = "SELECT COALESCE(SUM(rejected_qty), 0) FROM rail_ie_production_rejection", nativeQuery = true)
    long sumAllRejectedQty();

    @Query(value = "SELECT COALESCE(SUM(total_pieces_rejected), 0) FROM rail_ie_production_verification", nativeQuery = true)
    long sumAllTotalPiecesRejected();

    @Query(value = "SELECT COALESCE(SUM(total_pieces_produced), 0) FROM rail_ie_production_verification", nativeQuery = true)
    long sumAllTotalPiecesProduced();

    @Query(value = """
        SELECT 
            Prod.Month_Year,
            COALESCE(Rej.Total_Rejected, 0) AS Total_Rejected,
            COALESCE(Prod.Total_Produced, 0) AS Total_Produced,
            ROUND(COALESCE(Rej.Total_Rejected, 0) * 100.0 / NULLIF(COALESCE(Prod.Total_Produced, 0), 0), 2) AS Rejection_Percentage
        FROM (
            SELECT 
                DATE_FORMAT(IFNULL(v.casting_date, v.created_date), '%b-%y') AS Month_Year,
                YEAR(IFNULL(v.casting_date, v.created_date)) AS Y,
                MONTH(IFNULL(v.casting_date, v.created_date)) AS M,
                SUM(COALESCE(v.total_pieces_produced, 0)) AS Total_Produced
            FROM rail_ie_production_verification v
            WHERE IFNULL(v.casting_date, v.created_date) BETWEEN :startDate AND :endDate
            GROUP BY Y, M, Month_Year
        ) Prod
        LEFT JOIN (
            SELECT 
                DATE_FORMAT(IFNULL(v2.casting_date, v2.created_date), '%b-%y') AS Month_Year,
                YEAR(IFNULL(v2.casting_date, v2.created_date)) AS Y,
                MONTH(IFNULL(v2.casting_date, v2.created_date)) AS M,
                SUM(COALESCE(r.rejected_qty, 0)) AS Total_Rejected
            FROM rail_ie_production_rejection r
            JOIN rail_ie_production_verification v2 ON r.verification_id = v2.id
            WHERE IFNULL(v2.casting_date, v2.created_date) BETWEEN :startDate AND :endDate
            GROUP BY Y, M, Month_Year
        ) Rej ON Prod.Y = Rej.Y AND Prod.M = Rej.M
        ORDER BY Prod.Y ASC, Prod.M ASC
    """, nativeQuery = true)
    java.util.List<Object[]> findMonthlyRejectionTrend(
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, 
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
        SELECT r.reason AS reason, SUM(COALESCE(r.rejected_qty, 0)) AS count 
        FROM rail_ie_production_rejection r
        JOIN rail_ie_production_verification v ON r.verification_id = v.id
        WHERE IFNULL(v.casting_date, v.created_date) BETWEEN :startDate AND :endDate
        GROUP BY r.reason
        ORDER BY count DESC
    """, nativeQuery = true)
    java.util.List<Object[]> findRailPadParetoAnalysisRejections(
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate, 
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate);

    // ── Rail Pad Avg Production/Day (last 30 days, plant-based) ─────────────
    // Formula: SUM(pieces_produced) / COUNT(DISTINCT production_unit) / 24

    @Query(value = """
        SELECT COALESCE(SUM(v.total_pieces_produced), 0)
        FROM rail_ie_production_verification v
        WHERE v.casting_date >= :since
    """, nativeQuery = true)
    Long sumTotalPiecesProducedLast30Days(
        @org.springframework.data.repository.query.Param("since") java.time.LocalDate since);

    @Query(value = """
        SELECT COUNT(DISTINCT v.production_unit)
        FROM rail_ie_production_verification v
        WHERE v.casting_date >= :since
          AND v.total_pieces_produced > 0
          AND v.production_unit IS NOT NULL
    """, nativeQuery = true)
    Long countDistinctProductionUnitsLast30Days(
        @org.springframework.data.repository.query.Param("since") java.time.LocalDate since);

    // ── Rail Pad Accepted Qty by UOM (all time) ─────────────────────────────

    @Query(value = """
        SELECT pi_summary.uom, SUM(v_summary.accepted_qty)
        FROM (
            SELECT ph.po_no AS po_no, MIN(pi.uom) AS uom
            FROM po_item pi
            JOIN po_header ph ON pi.po_header_id = ph.id
            WHERE ph.item_cat_descr = 'Rail Pads'
            GROUP BY ph.po_no
        ) pi_summary
        JOIN (
            SELECT d.po_no AS po_no, SUM(COALESCE(v.total_accepted_pieces, 0)) AS accepted_qty
            FROM rail_ie_production_verification v
            JOIN rail_production_declaration d ON v.request_id = d.id
            GROUP BY d.po_no
        ) v_summary ON pi_summary.po_no COLLATE utf8mb4_unicode_ci = v_summary.po_no COLLATE utf8mb4_unicode_ci
        GROUP BY pi_summary.uom
    """, nativeQuery = true)
    java.util.List<Object[]> findAcceptedQtyByUom();

    // ── Rail Pad Shift Wise Production Report ─────────────────────────────────

    @Query(value = """
        SELECT 
            v.casting_date AS castingDate,
            v.shift AS shift,
            GROUP_CONCAT(DISTINCT d.po_no ORDER BY d.po_no SEPARATOR ', ') AS poNo,
            COALESCE(SUM(batch_counts.cnt), 0) AS noOfBatches,
            SUM(COALESCE(v.total_pieces_produced, 0)) AS producedQty,
            SUM(COALESCE(v.total_accepted_pieces, 0)) AS acceptedQty,
            SUM(COALESCE(v.total_pieces_rejected, 0)) AS rejectedQty,
            GROUP_CONCAT(DISTINCT d.vendor_name ORDER BY d.vendor_name SEPARATOR ', ') AS vendorName,
            GROUP_CONCAT(DISTINCT d.vendor_code ORDER BY d.vendor_code SEPARATOR ', ') AS vendorCode,
            GROUP_CONCAT(DISTINCT d.plant_id ORDER BY d.plant_id SEPARATOR ', ') AS plantId
        FROM rail_ie_production_verification v
        JOIN rail_production_declaration d ON v.request_id = d.id
        LEFT JOIN (
            SELECT p.declaration_id, COUNT(b.id) AS cnt
            FROM rail_production_batch b
            JOIN rail_production_product p ON b.product_id = p.id
            GROUP BY p.declaration_id
        ) batch_counts ON batch_counts.declaration_id = d.id
        WHERE v.casting_date BETWEEN :startDate AND :endDate
          AND (:vendorCode IS NULL OR :vendorCode = 'All Manufacturers' OR d.vendor_code = :vendorCode)
          AND (:plantId IS NULL OR :plantId = 'All Places' OR d.plant_id = :plantId)
        GROUP BY v.casting_date, v.shift
        ORDER BY v.casting_date DESC, v.shift ASC
    """, nativeQuery = true)
    java.util.List<Object[]> getRailPadShiftWiseProductionReport(
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate,
        @org.springframework.data.repository.query.Param("vendorCode") String vendorCode,
        @org.springframework.data.repository.query.Param("plantId") String plantId);

    @Query(value = """
        SELECT DISTINCT d.vendor_name, d.vendor_code
        FROM rail_production_declaration d
        WHERE d.vendor_name IS NOT NULL AND d.vendor_code IS NOT NULL
        ORDER BY d.vendor_name ASC
    """, nativeQuery = true)
    java.util.List<Object[]> findDistinctVendors();

    @Query(value = """
        SELECT DISTINCT d.plant_id
        FROM rail_production_declaration d
        WHERE d.plant_id IS NOT NULL
          AND (:vendorCode IS NULL OR :vendorCode = '' OR d.vendor_code = :vendorCode)
        ORDER BY d.plant_id ASC
    """, nativeQuery = true)
    java.util.List<String> findDistinctPlants(@org.springframework.data.repository.query.Param("vendorCode") String vendorCode);

    @Query(value = """
        SELECT 
            COALESCE(SUM(v.total_pieces_produced), 0) AS qtyInspected,
            COALESCE(SUM(v.total_accepted_pieces), 0) AS qtyAccepted
        FROM rail_ie_production_verification v
        JOIN rail_production_declaration d ON v.request_id = d.id
        WHERE d.po_no = :poNo
    """, nativeQuery = true)
    java.util.List<Object[]> findVerificationStatsByPo(@org.springframework.data.repository.query.Param("poNo") String poNo);

    @Query(value = """
        SELECT r.reason, COALESCE(SUM(r.rejected_qty), 0)
        FROM rail_ie_production_rejection r
        JOIN rail_ie_production_verification v ON r.verification_id = v.id
        JOIN rail_production_declaration d ON v.request_id = d.id
        WHERE d.po_no = :poNo
        GROUP BY r.reason
    """, nativeQuery = true)
    java.util.List<Object[]> findProcessRejectionsByPo(@org.springframework.data.repository.query.Param("poNo") String poNo);

    @Query(value = """
        SELECT 
            COALESCE(SUM(c.total_qty), 0) AS icIssuedQty,
            MAX(c.inspection_date) AS lastDateIcIssued
        FROM rail_inspection_call c
        WHERE c.po_no = :poNo
    """, nativeQuery = true)
    java.util.List<Object[]> findFinalInspectionStatsByPo(@org.springframework.data.repository.query.Param("poNo") String poNo);

    @Query(value = """
        SELECT 
            d.po_no AS poNo,
            COALESCE(SUM(v.total_pieces_produced), 0) AS qtyInspected,
            COALESCE(SUM(v.total_accepted_pieces), 0) AS qtyAccepted
        FROM rail_ie_production_verification v
        JOIN rail_production_declaration d ON v.request_id = d.id
        WHERE COALESCE(v.casting_date, DATE(v.created_date)) BETWEEN :startDate AND :endDate
        GROUP BY d.po_no
    """, nativeQuery = true)
    java.util.List<Object[]> findVerificationStatsGroupedByPo(
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @Query(value = """
        SELECT 
            c.po_no AS poNo,
            COALESCE(SUM(c.total_qty), 0) AS icIssuedQty,
            MAX(c.inspection_date) AS lastDateIcIssued
        FROM rail_inspection_call c
        WHERE c.inspection_date BETWEEN :startDate AND :endDate
        GROUP BY c.po_no
    """, nativeQuery = true)
    java.util.List<Object[]> findFinalInspectionStatsGroupedByPo(
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @Query(value = """
        SELECT 
            d.po_no AS poNo,
            r.reason AS reason,
            COALESCE(SUM(r.rejected_qty), 0) AS qty
        FROM rail_ie_production_rejection r
        JOIN rail_ie_production_verification v ON r.verification_id = v.id
        JOIN rail_production_declaration d ON v.request_id = d.id
        WHERE COALESCE(v.casting_date, DATE(v.created_date)) BETWEEN :startDate AND :endDate
        GROUP BY d.po_no, r.reason
    """, nativeQuery = true)
    java.util.List<Object[]> findProcessRejectionsGroupedByPo(
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);
}

