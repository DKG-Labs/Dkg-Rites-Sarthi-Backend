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
}

