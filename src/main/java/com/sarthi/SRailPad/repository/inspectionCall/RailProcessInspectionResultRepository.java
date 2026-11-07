package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailProcessInspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RailProcessInspectionResultRepository extends JpaRepository<RailProcessInspectionResult, Long> {
    Optional<RailProcessInspectionResult> findByInspectionCall_CallNo(String callNo);

    /**
     * Process inspection totals per PO (Rail Pad Quality Report).
     * Columns: [0] poNo, [1] inspectedNos, [2] acceptedNos, [3] inspectedSets, [4] acceptedSets
     */
    @Query(value = """
        SELECT
            SUBSTRING_INDEX(ic.po_no, '/', 1) AS poNo,
            COALESCE(SUM(r.total_manufactured_qty), 0) AS inspectedNos,
            COALESCE(SUM(r.total_accepted_qty), 0) AS acceptedNos,
            COALESCE(SUM(r.offered_sets), 0) AS inspectedSets,
            COALESCE(SUM(r.accepted_sets), 0) AS acceptedSets
        FROM rail_process_inspection_result r
        JOIN rail_inspection_call ic ON r.inspection_call_id = ic.id
        WHERE COALESCE(r.inspection_date, r.inspection_end_date, DATE(r.created_at)) BETWEEN :startDate AND :endDate
        GROUP BY SUBSTRING_INDEX(ic.po_no, '/', 1)
    """, nativeQuery = true)
    List<Object[]> findProcessQtyGroupedByPo(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * Process inspection batch rejections per PO and reason text (Rail Pad Quality Report).
     * reason_for_rejection may hold one or more "Reason (n Nos)" entries; parsed in service.
     * Columns: [0] poNo, [1] reasonText, [2] sumQtyRejected, [3] rowCount
     */
    @Query(value = """
        SELECT
            SUBSTRING_INDEX(ic.po_no, '/', 1) AS poNo,
            b.reason_for_rejection AS reason,
            COALESCE(SUM(b.qty_rejected), 0) AS qtyRejected,
            COUNT(*) AS rowCount
        FROM rail_process_inspection_batch b
        JOIN rail_process_inspection_result r ON b.result_id = r.id
        JOIN rail_inspection_call ic ON r.inspection_call_id = ic.id
        WHERE COALESCE(b.production_date, r.inspection_date, DATE(r.created_at)) BETWEEN :startDate AND :endDate
          AND COALESCE(b.qty_rejected, 0) > 0
        GROUP BY SUBSTRING_INDEX(ic.po_no, '/', 1), b.reason_for_rejection
    """, nativeQuery = true)
    List<Object[]> findProcessBatchRejectionsGroupedByPo(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);
}
