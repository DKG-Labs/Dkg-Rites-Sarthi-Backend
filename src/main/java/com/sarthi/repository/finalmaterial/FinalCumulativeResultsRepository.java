package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalCumulativeResults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Final Cumulative Results
 */
@Repository
public interface FinalCumulativeResultsRepository extends JpaRepository<FinalCumulativeResults, Long> {

    /**
     * Find cumulative results by inspection call number
     */
    Optional<FinalCumulativeResults> findByInspectionCallNo(String inspectionCallNo);

    /**
     * Find cumulative results by PO number
     */
    List<FinalCumulativeResults> findByPoNo(String poNo);

    /**
     * Check if cumulative results exist for a call
     */
    boolean existsByInspectionCallNo(String inspectionCallNo);
    /*
        @Query(value = """
                SELECT
                    p.id,
                    p.company_name,
                    p.poi_code,
                    u.username,
                    ip.rio,
                    'FINAL' AS stage,
                    SUM(f.qty_now_offered),
                    SUM(f.qty_now_passed),
                    SUM(f.qty_now_rejected)

                FROM final_cumulative_results f
                JOIN inspection_calls ic ON ic.ic_number = f.inspection_call_no
                JOIN pincode_poi_mapping p ON p.poi_code = ic.place_of_inspection
                LEFT JOIN ie_pincode_poi_mapping ipm ON ipm.poi_code = p.poi_code AND ipm.ie_type = 'PRIMARY'
                LEFT JOIN ie_profile ip ON ip.employee_code = ipm.employee_code
                LEFT JOIN po_header ph ON ph.po_no = f.po_no

                JOIN user_master u ON u.userid = f.created_by

                WHERE (:startDate IS NULL OR DATE(f.created_at) >= :startDate)
                  AND (:endDate IS NULL OR DATE(f.created_at) <= :endDate)
                  AND (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
                  AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
                  AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)

                GROUP BY
                    p.id,
                    p.company_name,
                    p.poi_code,
                    u.username,
                    ip.rio
                """, countQuery = "SELECT COUNT(*) FROM pincode_poi_mapping", nativeQuery = true)
        Page<Object[]> fetchFinal(@Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("rio") String rio,
                @Param("zone") String zone,
                @Param("vendor") String vendor,
                Pageable pageable);

     */
  /*  @Query(value = """
SELECT
    p.id,
    p.company_name,
    p.poi_code,
    u.username,
    ip.rio,
    'FINAL' AS stage,

    (f.accepted_qty + f.rejected_qty) AS inspected_qty,
    f.accepted_qty,
    f.rejected_qty

FROM (
    SELECT
        inspection_call_no,
        created_by,

        SUM(COALESCE(qty_now_passed,0)) AS accepted_qty,
        SUM(COALESCE(qty_now_rejected,0)) AS rejected_qty

    FROM final_cumulative_results
    WHERE (:startDate IS NULL OR DATE(created_at) >= :startDate)
      AND (:endDate IS NULL OR DATE(created_at) <= :endDate)

    GROUP BY inspection_call_no, created_by
) f

JOIN inspection_calls ic ON ic.ic_number = f.inspection_call_no
JOIN pincode_poi_mapping p ON p.poi_code = ic.place_of_inspection
LEFT JOIN ie_pincode_poi_mapping ipm 
    ON ipm.poi_code = p.poi_code AND ipm.ie_type = 'PRIMARY'
LEFT JOIN ie_profile ip 
    ON ip.employee_code = ipm.employee_code
LEFT JOIN po_header ph 
    ON ph.po_no = ic.po_no
JOIN user_master u 
    ON u.userid = f.created_by

WHERE (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
  AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
  AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)

GROUP BY
    p.id,
    p.company_name,
    p.poi_code,
    u.username,
    ip.rio,
    f.accepted_qty,
    f.rejected_qty
""",
            countQuery = "SELECT COUNT(*) FROM final_cumulative_results",
            nativeQuery = true)
    Page<Object[]> fetchFinal(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rio") String rio,
            @Param("zone") String zone,
            @Param("vendor") String vendor,
            Pageable pageable); */

    @Query(value = """
SELECT
    p.id,
    p.company_name,
    p.poi_code,
    u.username,
    ip.rio,
    'FINAL' AS stage,

    SUM(f.accepted_qty + f.rejected_qty) AS inspected_qty,
    SUM(f.accepted_qty) AS accepted_qty,
    SUM(f.rejected_qty) AS rejected_qty

FROM (

    SELECT
        inspection_call_no,
        created_by,
        SUM(COALESCE(qty_now_passed,0)) AS accepted_qty,
        SUM(COALESCE(qty_now_rejected,0)) AS rejected_qty
    FROM final_cumulative_results
    WHERE (:startDate IS NULL OR DATE(created_at) >= :startDate)
      AND (:endDate IS NULL OR DATE(created_at) <= :endDate)
    GROUP BY inspection_call_no, created_by
) f


JOIN inspection_calls ic 
    ON ic.ic_number = f.inspection_call_no

JOIN pincode_poi_mapping p 
    ON p.poi_code = ic.place_of_inspection


JOIN user_master u 
    ON u.userid = f.created_by

JOIN ie_profile ip 
    ON ip.employee_code = u.employee_code

LEFT JOIN po_header ph 
    ON ph.po_no = ic.po_no

WHERE (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
  AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
  AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)

GROUP BY
    p.id,
    p.company_name,
    p.poi_code,
    u.username,
    ip.rio
""",
            countQuery = """
SELECT COUNT(DISTINCT inspection_call_no, created_by)
FROM final_cumulative_results
""",
            nativeQuery = true)
    Page<Object[]> fetchFinal(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rio") String rio,
            @Param("zone") String zone,
            @Param("vendor") String vendor,
            Pageable pageable);

    /**
     * Aggregation query: computes cumulative sums directly in the database.
     * Uses native SQL with explicit COLLATE to handle collation mismatch between
     * final_cumulative_results (utf8mb4_unicode_ci) and inspection_calls
     * (utf8mb4_0900_ai_ci).
     * Returns a single Object[] row: [SUM(passed), SUM(rejected), SUM(offered)]
     */
    @Query(value = """
                SELECT
                    COALESCE(SUM(fcr.qty_now_passed), 0),
                    COALESCE(SUM(fcr.qty_now_rejected), 0),
                    COALESCE(SUM(fcr.qty_now_offered), 0)
                FROM final_cumulative_results fcr
                JOIN inspection_calls ic
                    ON ic.ic_number COLLATE utf8mb4_unicode_ci = fcr.inspection_call_no COLLATE utf8mb4_unicode_ci
                WHERE fcr.po_no = :poNo
                  AND ic.id != :currentCallId
                  AND ic.created_at <= :beforeOrAt
                  AND (
                      ic.po_serial_no COLLATE utf8mb4_unicode_ci = :serialNo
                      OR ic.po_serial_no COLLATE utf8mb4_unicode_ci LIKE CONCAT('%/', :serialNo)
                  )
            """, nativeQuery = true)
    Object[] sumCumulativeByPoNoAndSerialNoExcludingCall(
            @Param("poNo") String poNo,
            @Param("serialNo") String serialNo,
            @Param("currentCallId") Integer currentCallId,
            @Param("beforeOrAt") LocalDateTime beforeOrAt);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(fcr.qtyNowPassed) FROM FinalCumulativeResults fcr")
    Long sumTotalQtyNowPassed();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(fcr.qtyNowPassed) FROM FinalCumulativeResults fcr WHERE fcr.createdAt >= :date")
    Long sumTotalQtyNowPassedLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(fcr.qtyNowRejected), SUM(fcr.qtyNowOffered) FROM FinalCumulativeResults fcr WHERE fcr.createdAt >= :date")
    List<Object[]> sumFinalRejectionLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @Query("""
             SELECT
                 SUM(f.qtyNowPassed),
                 SUM(f.qtyNowRejected)
             FROM FinalCumulativeResults f
             WHERE f.inspectionCallNo IN :callNos
            """)
    List<Object[]> findFinalInspectionQty(@Param("callNos") List<String> callNos);

    @Query("SELECT COALESCE(SUM(f.qtyNowPassed), 0), COALESCE(SUM(f.qtyNowRejected), 0) FROM FinalCumulativeResults f")
    List<Object[]> sumFinalAcceptedAndRejected();

    @Query(value = """
        SELECT 
            SUM(COALESCE(f.qty_now_passed, 0)), 
            SUM(COALESCE(f.qty_now_rejected, 0)) 
        FROM final_cumulative_results f 
        WHERE (CASE WHEN f.date_of_inspection IS NOT NULL THEN DATE(f.date_of_inspection) ELSE DATE(f.created_at) END) BETWEEN :startDate AND :endDate
    """, nativeQuery = true)
    List<Object[]> sumFinalAcceptedAndRejectedRevisedLogic(
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

  /*  @Query(value = """

    SELECT
        ph.case_no                                   AS caseNumber,

        DATE(ic.created_at)                          AS callDate,

        ic.place_of_inspection                       AS placeOfInspection,

        CAST(um.employee_code AS CHAR)               AS ieEmployeeNumber,

        'IC Generated'                               AS callStatus,

        ic.po_serial_no                              AS poItemSerialNumber,

        CAST(f.book_no AS CHAR)                      AS bkNumber,

        CAST(f.set_no AS CHAR)                       AS setNumber,

        DATE(f.created_at)                           AS icDate,

        COALESCE(fr.qty_now_offered,0)
                                                        AS quantityOffered,

        COALESCE(fr.qty_now_passed,0)
                                                        AS quantityPassed,

        COALESCE(fr.qty_now_rejected,0)
                                                        AS quantityRejected,
        ic.ic_number                                    AS callNo

    FROM final_ic_edit f

    INNER JOIN inspection_calls ic
            ON ic.ic_number =
               SUBSTRING_INDEX(
                           SUBSTRING_INDEX(f.ic_number,'/',2),
                           '/',
                           -1
                       )

    INNER JOIN po_header ph
            ON ph.po_no = ic.po_no

    INNER JOIN user_master um
            ON um.userid = f.created_by

    LEFT JOIN final_cumulative_results fr
            ON fr.inspection_call_no = ic.ic_number

    LEFT JOIN ibs_call_registration icr
            ON icr.call_number = ic.ic_number

    WHERE icr.call_number IS NULL
       OR icr.status = 'Failed'

    """,
            nativeQuery = true)
    List<Object[]> getFinalInspectionCalls();*/

    @Query(value = """

SELECT
    ph.case_no                                   AS caseNumber,

    DATE(ic.created_at)                          AS callDate,

    ic.place_of_inspection                       AS placeOfInspection,

    CAST(um.employee_code AS CHAR)               AS ieEmployeeNumber,

    'A'                                          AS callStatus,
    
    'F'                                          AS typeOfCall,

    ic.po_serial_no                              AS poItemSerialNumber,

    CAST(f.book_no AS CHAR)                      AS bkNumber,

    CAST(f.set_no AS CHAR)                       AS setNumber,

    DATE(f.created_at)                           AS icDate,

    COALESCE(fr.qty_now_offered,0)
                                                    AS quantityOffered,

    COALESCE(fr.qty_now_passed,0)
                                                    AS quantityPassed,

    COALESCE(fr.qty_now_rejected,0)
                                                    AS quantityRejected,

    ic.ic_number                                 AS callNo

FROM final_ic_edit f

INNER JOIN inspection_calls ic
        ON ic.ic_number COLLATE utf8mb4_unicode_ci =
           SUBSTRING_INDEX(
                SUBSTRING_INDEX(f.ic_number,'/',2),
                '/',
                -1
           ) COLLATE utf8mb4_unicode_ci

INNER JOIN po_header ph
        ON ph.po_no = ic.po_no

INNER JOIN user_master um
        ON um.userid = f.created_by

LEFT JOIN final_cumulative_results fr
        ON fr.inspection_call_no COLLATE utf8mb4_unicode_ci
         = ic.ic_number COLLATE utf8mb4_unicode_ci

LEFT JOIN ibs_call_registration icr
        ON icr.call_number COLLATE utf8mb4_unicode_ci
         = ic.ic_number COLLATE utf8mb4_unicode_ci

WHERE icr.call_number IS NULL
   OR icr.status = 'Failed'

""",
            nativeQuery = true)
    List<Object[]> getFinalInspectionCalls();

    @Query(value = """
    SELECT
        inspection_call_no,
        qty_now_offered,
        qty_now_passed
    FROM final_cumulative_results
    WHERE inspection_call_no IN (:callNos)
""", nativeQuery = true)
    List<Object[]> findFinalSummaryByCallNos(@Param("callNos") List<String> callNos);
}