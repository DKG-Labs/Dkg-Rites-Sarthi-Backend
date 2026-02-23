package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalCumulativeResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
