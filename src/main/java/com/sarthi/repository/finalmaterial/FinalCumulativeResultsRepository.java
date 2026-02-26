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


    @Query(value = """
        SELECT 
            p.id,
            p.company_name,
            p.poi_code,
            u.username,
            ru.rio,
            'FINAL' AS stage,
            SUM(f.qty_now_offered),
            SUM(f.qty_now_passed),
            SUM(f.qty_now_rejected)

        FROM pincode_poi_mapping p

        JOIN ie_pincode_poi_mapping ip
             ON ip.poi_code = p.poi_code

        JOIN user_master u
             ON u.EMPLOYEE_ID = ip.employee_code

        LEFT JOIN ie_fields_mapping ru
             ON ru.pin_code = p.pin_code

        JOIN final_cumulative_results f
             ON f.created_by = u.userid
        WHERE (:startDate IS NULL OR DATE(f.created_at) >= :startDate)
        AND (:endDate IS NULL OR DATE(f.created_at) <= :endDate)

        GROUP BY 
            p.id,
            p.company_name,
            p.poi_code,
            u.username,
            ru.rio
        """,
            countQuery = "SELECT COUNT(*) FROM pincode_poi_mapping",
            nativeQuery = true)
    Page<Object[]> fetchFinal(@Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate, Pageable pageable);



    /**
     * Aggregation query: computes cumulative sums directly in the database.
     * Uses native SQL with explicit COLLATE to handle collation mismatch between
     * final_cumulative_results (utf8mb4_unicode_ci) and inspection_calls (utf8mb4_0900_ai_ci).
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
        @Param("beforeOrAt") LocalDateTime beforeOrAt
    );

}
