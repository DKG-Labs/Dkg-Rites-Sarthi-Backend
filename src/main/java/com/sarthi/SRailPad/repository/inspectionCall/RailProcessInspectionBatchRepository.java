package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailProcessInspectionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailProcessInspectionBatchRepository extends JpaRepository<RailProcessInspectionBatch, Long> {

    List<RailProcessInspectionBatch> findByDeclarationBatchId(Long declarationBatchId);

    @Query("SELECT b FROM RailProcessInspectionBatch b WHERE b.declarationBatchId IN :declarationBatchIds AND (b.result.inspectionCall.callNo <> :excludeCallNo OR :excludeCallNo IS NULL OR :excludeCallNo = '')")
    List<RailProcessInspectionBatch> findByDeclarationBatchIdInAndExcludeCallNo(
            @Param("declarationBatchIds") List<Long> declarationBatchIds,
            @Param("excludeCallNo") String excludeCallNo);

    /**
     * Usage left on batch ids that no longer exist (IE verification unblocked / re-saved gives the batch a new id).
     * Matched later by production date + batch no (+ drawing) to the current batch row of the same PO.
     */
    @Query("SELECT b FROM RailProcessInspectionBatch b WHERE b.productionDate IN :dates AND b.batchNo IN :batchNos " +
           "AND b.result.inspectionCall.poNo = :poNo " +
           "AND (b.result.inspectionCall.callNo <> :excludeCallNo OR :excludeCallNo IS NULL OR :excludeCallNo = '') " +
           "AND (b.declarationBatchId IS NULL OR b.declarationBatchId NOT IN :liveIds)")
    List<RailProcessInspectionBatch> findUsageOnDeletedBatchIds(
            @Param("poNo") String poNo,
            @Param("dates") java.util.Collection<java.time.LocalDate> dates,
            @Param("batchNos") java.util.Collection<String> batchNos,
            @Param("liveIds") java.util.Collection<Long> liveIds,
            @Param("excludeCallNo") String excludeCallNo);
}

