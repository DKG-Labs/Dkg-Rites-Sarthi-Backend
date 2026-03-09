package com.sarthi.repository.rawmaterial;

import com.sarthi.dto.InspectionDataDto;
import com.sarthi.entity.rawmaterial.InspectionCall;
import org.hibernate.query.SelectionQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for InspectionCall entity.
 * Provides CRUD operations and custom queries for inspection calls.
 */
@Repository
public interface InspectionCallRepository extends JpaRepository<InspectionCall, Integer> {

    /* ==================== Find by IC Number ==================== */

    Optional<InspectionCall> findByIcNumber(String icNumber);

    /**
     * Batch fetch inspection calls by IC numbers (for performance optimization)
     */
    List<InspectionCall> findByIcNumberIn(List<String> icNumbers);

    /* ==================== Find by Status ==================== */

    List<InspectionCall> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    List<InspectionCall> findByStatusInOrderByCreatedAtDesc(List<String> statuses);

    /* ==================== Find by Type of Call ==================== */

    List<InspectionCall> findByTypeOfCallOrderByCreatedAtDesc(String typeOfCall);

    List<InspectionCall> findByTypeOfCallAndStatusIgnoreCaseOrderByCreatedAtDesc(
            String typeOfCall, String status);

    /* ==================== Find by PO Number ==================== */

    List<InspectionCall> findByPoNoOrderByCreatedAtDesc(String poNo);

    /* ==================== Find by Company ==================== */

    List<InspectionCall> findByCompanyNameContainingIgnoreCaseOrderByCreatedAtDesc(String companyName);

    /* ==================== Find by Vendor ID ==================== */

    List<InspectionCall> findByVendorIdOrderByCreatedAtDesc(String vendorId);

    /* ==================== Custom Queries ==================== */

    /**
     * Find all Raw Material inspection calls with details eagerly loaded
     */
    @Query("SELECT DISTINCT ic FROM InspectionCall ic " +
            "LEFT JOIN FETCH ic.rmInspectionDetails " +
            "WHERE ic.typeOfCall = 'Raw Material' " +
            "ORDER BY ic.createdAt DESC")
    List<InspectionCall> findAllRawMaterialCallsWithDetails();

    /**
     * Find Raw Material calls by status with details
     */
    @Query("SELECT DISTINCT ic FROM InspectionCall ic " +
            "LEFT JOIN FETCH ic.rmInspectionDetails " +
            "WHERE ic.typeOfCall = 'Raw Material' AND UPPER(ic.status) = UPPER(:status) " +
            "ORDER BY ic.createdAt DESC")
    List<InspectionCall> findRawMaterialCallsByStatusWithDetails(@Param("status") String status);

    /**
     * Count calls by status and type
     */
    @Query("SELECT COUNT(ic) FROM InspectionCall ic " +
            "WHERE ic.typeOfCall = :type AND UPPER(ic.status) = UPPER(:status)")
    long countByTypeAndStatus(@Param("type") String type, @Param("status") String status);

    /**
     * Count calls by type
     */
    @Query("SELECT COUNT(ic) FROM InspectionCall ic WHERE ic.typeOfCall = :type")
    long countByTypeOfCall(@Param("type") String type);

    /**
     * Count calls by type and created date (for daily sequence)
     * Counts ICs created on a specific date for a given type
     */
    @Query("SELECT COUNT(ic) FROM InspectionCall ic " +
            "WHERE ic.typeOfCall = :type " +
            "AND FUNCTION('DATE', ic.createdAt) = :date")
    long countByTypeOfCallAndCreatedDate(@Param("type") String type, @Param("date") java.time.LocalDate date);

    /**
     * Check if IC number exists
     */
    boolean existsByIcNumber(String icNumber);

    @Query("""
            SELECT new com.sarthi.dto.InspectionDataDto(
                ic.icNumber,
                ic.poNo,
                ic.vendorId,
                ic.companyName,
                ic.typeOfCall,
                ic.desiredInspectionDate,
                ic.placeOfInspection,
                ic.poSerialNo,
                null, -- origDp
                null, -- extDp
                null  -- rlyShortName
            )
            FROM InspectionCall ic
            WHERE ic.icNumber IN :icNumbers
            """)
    List<InspectionDataDto> findLiteByIcNumberIn(
            @Param("icNumbers") List<String> icNumbers);

    // @Query("SELECT ic.icNumber FROM InspectionCall ic WHERE ic.poSerialNo =
    // :poSerialNo")
    // List<String> findCallNumbersByPoNo(@Param("poSerialNo") String poSerialNo);

    @Query("""
            SELECT ic.icNumber
            FROM InspectionCall ic
            WHERE ic.poSerialNo LIKE CONCAT('%/', :poSerialNo)
            """)
    List<String> findCallNumbersByPoNo(@Param("poSerialNo") String poSerialNo);

    @Query("""
            SELECT ic.icNumber
            FROM InspectionCall ic
            WHERE ic.poNo = :poNo
            """)
    List<String> findCallNumbersByPo(@Param("poNo") String poNo);

    @Query("""
                SELECT
                    CASE
                        WHEN SUM(r.weightOfferedMt) = 0 THEN 0.0
                        ELSE (SUM(r.weightRejectedMt) * 100.0) / SUM(r.weightOfferedMt)
                    END
                FROM InspectionCall ic
                JOIN RmHeatFinalResult r
                    ON r.inspectionCallNo = ic.icNumber
                WHERE ic.poNo = :poNo
            """)
    Double findRmRejectionPct(@Param("poNo") String poNo);

    @Query("""
                SELECT ic.icNumber
                FROM InspectionCall ic
                WHERE ic.poNo = :poNo
                  AND ic.poSerialNo LIKE CONCAT('%/', :poSerialNo)
            """)
    List<String> findCallNosByPoAndSerial(
            @Param("poNo") String poNo,
            @Param("poSerialNo") String poSerialNo);

    @Query("""
                SELECT ic
                FROM InspectionCall ic
                WHERE ic.poSerialNo LIKE CONCAT('%/', :serialNo)
            """)
    List<InspectionCall> findBySerialNo(@Param("serialNo") String serialNo);

    @Query("""
                SELECT ic
                FROM InspectionCall ic
                WHERE ic.poNo = :poNo
                  AND ic.poSerialNo LIKE CONCAT('%/', :serialNo)
            """)
    Page<InspectionCall> findByPoNoAndSerialNo(
            @Param("poNo") String poNo,
            @Param("serialNo") String serialNo,
            Pageable pageable);

    @Query("""
                SELECT ic
                FROM InspectionCall ic
                WHERE ic.poNo = :poNo
                  AND ic.poSerialNo LIKE CONCAT('%/', :serialNo)
            """)
    List<InspectionCall> findByPoNoAndSerialNo(
            @Param("poNo") String poNo,
            @Param("serialNo") String serialNo);

    /**
     * Check if an inspection call already exists for a given PO Serial No
     *
     * @param poSerialNo - PO Serial Number to check
     * @return true if at least one inspection call exists, false otherwise
     */
    @Query("""
                SELECT CASE WHEN COUNT(ic) > 0 THEN true ELSE false END
                FROM InspectionCall ic
                WHERE ic.poSerialNo LIKE CONCAT('%/', :poSerialNo)
            """)
    boolean existsByPoSerialNo(@Param("poSerialNo") String poSerialNo);

    @Query("SELECT COUNT(ic) FROM InspectionCall ic WHERE ic.poNo = :poNo")
    long countByPoNo(@Param("poNo") String poNo);

    @Query("SELECT COUNT(ic) FROM InspectionCall ic WHERE ic.poNo = :poNo AND UPPER(ic.status) IN :statuses")
    long countByPoNoAndStatusIn(@Param("poNo") String poNo, @Param("statuses") List<String> statuses);

    /*
     * 
     * @Query(value = """
     * SELECT
     * ph.rly_short_name,
     * ic.po_no,
     * ph.firm_details,
     * 
     * COALESCE((
     * SELECT SUM(pi.qty)
     * FROM po_item pi
     * WHERE pi.po_header_id = ph.id
     * ),0) AS poQty,
     * 
     * COALESCE((
     * SELECT SUM(r.accepted_qty_mt)
     * FROM rm_heat_final_result r
     * WHERE r.inspection_call_no IN (
     * SELECT ic2.ic_number
     * FROM inspection_calls ic2
     * WHERE ic2.po_no = ic.po_no
     * AND ic2.created_at BETWEEN :startDate AND :endDate
     * )
     * ),0) AS monthlyRm,
     * 
     * COALESCE((
     * SELECT SUM(pq.INSPECTED_QTY)
     * FROM process_ie_qty pq
     * WHERE pq.REQUEST_ID IN (
     * SELECT ic2.ic_number
     * FROM inspection_calls ic2
     * WHERE ic2.po_no = ic.po_no
     * AND ic2.created_at BETWEEN :startDate AND :endDate
     * )
     * ),0) AS monthlyProcess,
     * 
     * COALESCE((
     * SELECT SUM(f.qty_now_passed)
     * FROM final_cumulative_results f
     * WHERE f.inspection_call_no IN (
     * SELECT ic2.ic_number
     * FROM inspection_calls ic2
     * WHERE ic2.po_no = ic.po_no
     * AND ic2.created_at BETWEEN :startDate AND :endDate
     * )
     * ),0) AS monthlyFinal,
     * 
     * COALESCE((
     * SELECT SUM(f.qty_now_passed)
     * FROM final_cumulative_results f
     * WHERE f.inspection_call_no IN (
     * SELECT ic3.ic_number
     * FROM inspection_calls ic3
     * WHERE ic3.po_no = ic.po_no
     * )
     * ),0) AS totalFinalInspected
     * 
     * FROM (
     * SELECT DISTINCT po_no
     * FROM inspection_calls
     * WHERE created_at BETWEEN :startDate AND :endDate
     * ) ic
     * 
     * LEFT JOIN po_header ph
     * ON ph.po_no COLLATE utf8mb4_unicode_ci = ic.po_no COLLATE utf8mb4_unicode_ci
     * 
     * """,
     * countQuery = """
     * SELECT COUNT(DISTINCT po_no)
     * FROM inspection_calls
     * WHERE created_at BETWEEN :startDate AND :endDate
     * """,
     * nativeQuery = true)
     * Page<Object[]> fetchMonthlyProgress(
     * 
     * @Param("startDate") LocalDate startDate,
     * 
     * @Param("endDate") LocalDate endDate,
     * Pageable pageable);
     */
    @Query(value = """
            SELECT
                ph.rly_short_name,
                ic.po_no,
                ph.firm_details,

                COALESCE((
                    SELECT SUM(pi.qty)
                    FROM po_item pi
                    WHERE pi.po_header_id = ph.id
                ),0) AS poQty,

                COALESCE((
                    SELECT SUM(r.accepted_qty_mt)
                    FROM rm_heat_final_result r
                    WHERE r.inspection_call_no IN (
                        SELECT ic2.ic_number
                        FROM inspection_calls ic2
                        WHERE ic2.po_no = ic.po_no
                          AND ic2.created_at BETWEEN :startDate AND :endDate
                    )
                ),0) AS monthlyRm,

                COALESCE((
                    SELECT SUM(pq.INSPECTED_QTY)
                    FROM process_ie_qty pq
                    WHERE pq.REQUEST_ID IN (
                        SELECT ic2.ic_number
                        FROM inspection_calls ic2
                        WHERE ic2.po_no = ic.po_no
                          AND ic2.created_at BETWEEN :startDate AND :endDate
                    )
                ),0) AS monthlyProcess,

                COALESCE((
                    SELECT SUM(f.qty_now_passed)
                    FROM final_cumulative_results f
                    WHERE f.inspection_call_no IN (
                        SELECT ic2.ic_number
                        FROM inspection_calls ic2
                        WHERE ic2.po_no = ic.po_no
                          AND ic2.created_at BETWEEN :startDate AND :endDate
                    )
                ),0) AS monthlyFinal,

                --  TOTAL FINAL WITHOUT DATE FILTER
                COALESCE((
                    SELECT SUM(f.qty_now_passed)
                    FROM final_cumulative_results f
                    WHERE f.po_no COLLATE utf8mb4_unicode_ci = ic.po_no COLLATE utf8mb4_unicode_ci
                ),0) AS totalFinalInspected

            FROM (
                SELECT DISTINCT po_no
                FROM inspection_calls
                WHERE created_at BETWEEN :startDate AND :endDate
            ) ic

            LEFT JOIN po_header ph
                ON ph.po_no COLLATE utf8mb4_unicode_ci = ic.po_no COLLATE utf8mb4_unicode_ci
            """, countQuery = """
                SELECT COUNT(DISTINCT po_no)
                FROM inspection_calls
                WHERE created_at BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    Page<Object[]> fetchMonthlyProgress(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query(value = """
            SELECT
                ic.company_name AS manufacturer,

                COALESCE(SUM(
                    CASE
                        WHEN f.qty_now_offered IS NOT NULL
                             THEN f.qty_now_offered
                        ELSE p.manufacture_qty
                    END
                ),0) AS manufactured,

                COALESCE(SUM(f.qty_now_passed),0) AS inspected,
                COALESCE(SUM(f.qty_now_rejected),0) AS rejected,

                COALESCE(SUM(r.weight_rejected_mt),0) AS rmRejected,
                COALESCE(SUM(p.rejected_qty),0) AS processRejected,
                COALESCE(SUM(f.qty_now_rejected),0) AS finalRejected

            FROM inspection_calls ic

            LEFT JOIN final_cumulative_results f
                   ON f.inspection_call_no = ic.ic_number

            LEFT JOIN rm_heat_final_result r
                   ON r.inspection_call_no = ic.ic_number

            LEFT JOIN process_ie_qty p
                   ON p.REQUEST_ID = ic.ic_number

            WHERE ic.created_at BETWEEN :startDate AND :endDate

            GROUP BY ic.company_name
            """, countQuery = """
            SELECT COUNT(DISTINCT company_name)
            FROM inspection_calls
            WHERE created_at BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    Page<Object[]> fetchManufacturerSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query(value = """
            SELECT DISTINCT ic.ic_number
            FROM ie_poi_mapping ipm
            JOIN inspection_calls ic
                ON ic.place_of_inspection = ipm.POI_CODE
            WHERE ipm.IE_USER_ID = :userId
            AND ic.type_of_call = 'process'
            """, nativeQuery = true)
    List<String> findIcNumbersByUserId(@Param("userId") Long userId);
}
