package com.sarthi.repository.rawmaterial;

import com.sarthi.dto.InspectionDataDto;
import com.sarthi.entity.rawmaterial.InspectionCall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"rmInspectionDetails", "processInspectionDetails", "finalInspectionDetails"})
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
                ic.poSerialNo,
                ic.vendorId,
                ic.typeOfCall,
                ic.desiredInspectionDate,
                ic.placeOfInspection,
                ic.unitAddress,
                ic.companyName,
                pi.deliveryDate,
                pi.extendedDeliveryDate
            )
            FROM InspectionCall ic
            JOIN PoHeader ph ON ic.poNo = ph.poNo
            JOIN ph.items pi ON ic.poSerialNo LIKE CONCAT('%/', pi.itemSrNo)
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
                SELECT DISTINCT ic_sub.po_no
                FROM inspection_calls ic_sub

                JOIN pincode_poi_mapping p ON p.poi_code = ic_sub.place_of_inspection
                LEFT JOIN ie_pincode_poi_mapping ipm ON ipm.poi_code = p.poi_code AND ipm.ie_type = 'PRIMARY'
                LEFT JOIN ie_profile ip ON ip.employee_code = ipm.employee_code
                LEFT JOIN po_header ph_sub ON ph_sub.po_no = ic_sub.po_no

                WHERE (:startDate IS NULL OR ic_sub.created_at >= :startDate)
                  AND (:endDate IS NULL OR ic_sub.created_at <= :endDate)
                  AND (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
                  AND (:zone IS NULL OR :zone = '' OR ph_sub.rly_short_name = :zone)
                  AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)
            ) ic

            LEFT JOIN po_header ph
                ON ph.po_no COLLATE utf8mb4_unicode_ci = ic.po_no COLLATE utf8mb4_unicode_ci
            """, countQuery = """
                SELECT COUNT(DISTINCT ic_sub.po_no)
                FROM inspection_calls ic_sub
                JOIN pincode_poi_mapping p ON p.poi_code = ic_sub.place_of_inspection
                LEFT JOIN ie_pincode_poi_mapping ipm ON ipm.poi_code = p.poi_code AND ipm.ie_type = 'PRIMARY'
                LEFT JOIN ie_profile ip ON ip.employee_code = ipm.employee_code
                LEFT JOIN po_header ph_sub ON ph_sub.po_no = ic_sub.po_no
                WHERE (:startDate IS NULL OR ic_sub.created_at >= :startDate)
                  AND (:endDate IS NULL OR ic_sub.created_at <= :endDate)
                  AND (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
                  AND (:zone IS NULL OR :zone = '' OR ph_sub.rly_short_name = :zone)
                  AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)
            """, nativeQuery = true)
    Page<Object[]> fetchMonthlyProgress(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rio") String rio,
            @Param("zone") String zone,
            @Param("vendor") String vendor,
            Pageable pageable);
/*
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

            JOIN pincode_poi_mapping poi ON poi.poi_code = ic.place_of_inspection
            LEFT JOIN ie_pincode_poi_mapping ipm ON ipm.poi_code = poi.poi_code AND ipm.ie_type = 'PRIMARY'
            LEFT JOIN ie_profile ip ON ip.employee_code = ipm.employee_code
            LEFT JOIN po_header ph ON ph.po_no = ic.po_no

            WHERE (:startDate IS NULL OR ic.created_at >= :startDate)
              AND (:endDate IS NULL OR ic.created_at <= :endDate)
              AND (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
              AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
              AND (:vendor IS NULL OR :vendor = '' OR poi.company_name = :vendor)

            GROUP BY ic.company_name
            """, countQuery = """
            SELECT COUNT(DISTINCT ic.company_name)
            FROM inspection_calls ic
            JOIN pincode_poi_mapping poi ON poi.poi_code = ic.place_of_inspection
            LEFT JOIN ie_pincode_poi_mapping ipm ON ipm.poi_code = poi.poi_code AND ipm.ie_type = 'PRIMARY'
            LEFT JOIN ie_profile ip ON ip.employee_code = ipm.employee_code
            LEFT JOIN po_header ph ON ph.po_no = ic.po_no
            WHERE (:startDate IS NULL OR ic.created_at >= :startDate)
              AND (:endDate IS NULL OR ic.created_at <= :endDate)
              AND (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
              AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
              AND (:vendor IS NULL OR :vendor = '' OR poi.company_name = :vendor)
            """, nativeQuery = true)
    Page<Object[]> fetchManufacturerSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rio") String rio,
            @Param("zone") String zone,
            @Param("vendor") String vendor,
            Pageable pageable);*/
@Query(value = """
        SELECT
            poi.company_name AS manufacturer,

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

        JOIN pincode_poi_mapping poi
               ON poi.poi_code = ic.place_of_inspection

        WHERE ic.created_at BETWEEN :startDate AND :endDate

        GROUP BY poi.company_name
        """,
        countQuery = """
        SELECT COUNT(DISTINCT poi.company_name)
        FROM inspection_calls ic
        JOIN pincode_poi_mapping poi
               ON poi.poi_code = ic.place_of_inspection
        WHERE ic.created_at BETWEEN :startDate AND :endDate
        """,
        nativeQuery = true)
Page<Object[]> fetchManufacturerSummary(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable);

    @Query(value = """
            SELECT DISTINCT ic.ic_number
            FROM inspection_calls ic
            JOIN ie_poi_mapping ipm
                ON ic.place_of_inspection = ipm.POI_CODE
            WHERE ic.type_of_call = 'process'
            AND (
                    ipm.IE_USER_ID = :userId
                 OR ipm.IE_USER_ID IN (
                        SELECT IE_USER_ID
                        FROM process_ie_users
                        WHERE PROCESS_USER_ID = :userId
                    )
                )
            """, nativeQuery = true)
    List<String> findIcNumbersByUserId(@Param("userId") Long userId);



    @Query(value = """
    SELECT place_of_inspection 
    FROM inspection_calls 
    WHERE ic_number = :callNo
""", nativeQuery = true)
    String findPoiByCallNo(@Param("callNo") String callNo);


    @Query(value = """
    SELECT place_of_inspection
    FROM inspection_calls
    WHERE ic_number = :icNumber
""", nativeQuery = true)
    String findPlaceOfInspectionByIcNumber(@Param("icNumber") String icNumber);

    @Query(value = """
        SELECT
            DATE_FORMAT(ic.created_at, '%Y-%m') AS month,

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

        JOIN pincode_poi_mapping poi
               ON poi.poi_code = ic.place_of_inspection

        WHERE ic.created_at BETWEEN :startDate AND :endDate
          AND poi.company_name = :companyName

        GROUP BY DATE_FORMAT(ic.created_at, '%Y-%m')
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> fetchCompanyMonthWiseSummary(
            LocalDate startDate,
            LocalDate endDate,
            String companyName);

    @Query(value = """

SELECT
    poi.company_name AS manufacturer,
    DATE_FORMAT(ic.created_at, '%Y-%m') AS month,

    -- PROCESS TOTALS
    COALESCE(SUM(pl.total_manufactured),0) AS inspected,

    COALESCE(SUM(pl.shearing_accepted),0) AS accepted,

    COALESCE(SUM(pl.total_rejected),0) AS processRejected,

    CASE 
        WHEN SUM(pl.total_manufactured) = 0 THEN 0
        ELSE (SUM(pl.total_rejected) * 100.0 / SUM(pl.total_manufactured))
    END AS processRejPercent,

    -- SHEARING
    COALESCE(SUM(psd.length_cut_bar_rejected),0),
    COALESCE(SUM(psd.improper_dia_rejected),0),
    COALESCE(SUM(psd.sharp_edges_rejected),0),
    COALESCE(SUM(psd.cracked_edges_rejected),0),

    -- TURNING
    COALESCE(SUM(ptd.parallel_length_rejected),0),
    COALESCE(SUM(ptd.full_turning_length_rejected),0),
    COALESCE(SUM(ptd.turning_dia_rejected),0),

    -- FORGING
    COALESCE(SUM(pfd.forging_temp_rejected),0),
    COALESCE(SUM(pfd.forging_stabilisation_rejection_rejected),0),
    COALESCE(SUM(pfd.improper_forging_rejected),0),
    COALESCE(SUM(pfd.forging_defect_rejected),0),
    COALESCE(SUM(pfd.embossing_defect_rejected),0),

    -- TEMPERING
    COALESCE(SUM(tpd.tempering_temperature_rejected),0),
    COALESCE(SUM(tpd.tempering_duration_rejected),0),

    -- QUENCHING
    COALESCE(SUM(qd.quenching_temperature_rejected),0),
    COALESCE(SUM(qd.quenching_duration_rejected),0),
    COALESCE(SUM(qd.quenching_hardness_rejected),0),
    COALESCE(SUM(qd.box_gauge_rejected),0),
    COALESCE(SUM(qd.flat_bearing_area_rejected),0),

    -- MPI
    COALESCE(SUM(mpi.mpi_rejected),0),

    -- FINISHING
    COALESCE(SUM(fd.paint_identification_rejected),0),
    COALESCE(SUM(fd.erc_coating_rejected),0)

FROM inspection_calls ic

JOIN pincode_poi_mapping poi
    ON poi.poi_code = ic.place_of_inspection

-- AGGREGATED PL (NO INFLATION)
LEFT JOIN (
    SELECT inspection_call_no,
           SUM(total_manufactured) AS total_manufactured,
           SUM(total_rejected) AS total_rejected,
            SUM(shearing_accepted) AS shearing_accepted
    FROM process_line_final_result
    GROUP BY inspection_call_no
) pl ON pl.inspection_call_no = ic.ic_number

-- SHEARING
LEFT JOIN (
    SELECT inspection_call_no,
           SUM(length_cut_bar_rejected) AS length_cut_bar_rejected,
           SUM(improper_dia_rejected) AS improper_dia_rejected,
           SUM(sharp_edges_rejected) AS sharp_edges_rejected,
           SUM(cracked_edges_rejected) AS cracked_edges_rejected
    FROM process_shearing_data
    GROUP BY inspection_call_no
) psd ON psd.inspection_call_no = ic.ic_number

-- TURNING
LEFT JOIN (
    SELECT inspection_call_no,
           SUM(parallel_length_rejected) AS parallel_length_rejected,
           SUM(full_turning_length_rejected) AS full_turning_length_rejected,
           SUM(turning_dia_rejected) AS turning_dia_rejected
    FROM process_turning_data
    GROUP BY inspection_call_no
) ptd ON ptd.inspection_call_no = ic.ic_number

-- FORGING
LEFT JOIN (
    SELECT inspection_call_no,
           SUM(forging_temp_rejected) AS forging_temp_rejected,
           SUM(forging_stabilisation_rejection_rejected) AS forging_stabilisation_rejection_rejected,
           SUM(improper_forging_rejected) AS improper_forging_rejected,
           SUM(forging_defect_rejected) AS forging_defect_rejected,
           SUM(embossing_defect_rejected) AS embossing_defect_rejected
    FROM process_forging_data
    GROUP BY inspection_call_no
) pfd ON pfd.inspection_call_no = ic.ic_number

-- TEMPERING
LEFT JOIN (
    SELECT inspection_call_no,
           SUM(tempering_temperature_rejected) AS tempering_temperature_rejected,
           SUM(tempering_duration_rejected) AS tempering_duration_rejected
    FROM process_tempering_data
    GROUP BY inspection_call_no
) tpd ON tpd.inspection_call_no = ic.ic_number

-- QUENCHING
LEFT JOIN (
    SELECT inspection_call_no,
           SUM(quenching_temperature_rejected) AS quenching_temperature_rejected,
           SUM(quenching_duration_rejected) AS quenching_duration_rejected,
           SUM(quenching_hardness_rejected) AS quenching_hardness_rejected,
           SUM(box_gauge_rejected) AS box_gauge_rejected,
           SUM(flat_bearing_area_rejected) AS flat_bearing_area_rejected
    FROM process_quenching_data
    GROUP BY inspection_call_no
) qd ON qd.inspection_call_no = ic.ic_number

-- MPI
LEFT JOIN (
    SELECT inspection_call_no,
           SUM(mpi_rejected) AS mpi_rejected
    FROM process_mpi_data
    GROUP BY inspection_call_no
) mpi ON mpi.inspection_call_no = ic.ic_number

-- FINISHING
LEFT JOIN (
    SELECT inspection_call_no,
           SUM(paint_identification_rejected) AS paint_identification_rejected,
           SUM(erc_coating_rejected) AS erc_coating_rejected
    FROM process_testing_finishing_data
    GROUP BY inspection_call_no
) fd ON fd.inspection_call_no = ic.ic_number

WHERE ic.created_at BETWEEN :startDate AND :endDate
  AND poi.company_name = :companyName

GROUP BY 
    poi.company_name,
    DATE_FORMAT(ic.created_at, '%Y-%m')

ORDER BY month

""", nativeQuery = true)
    List<Object[]> fetchProcessMonthWiseData(
            LocalDate startDate,
            LocalDate endDate,
            String companyName);
}

