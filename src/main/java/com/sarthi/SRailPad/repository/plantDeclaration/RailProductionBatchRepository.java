package com.sarthi.SRailPad.repository.plantDeclaration;

import com.sarthi.SRailPad.entity.plantDeclaration.RailProductionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface RailProductionBatchRepository extends JpaRepository<RailProductionBatch, Long> {

    @Query("SELECT b FROM RailProductionBatch b " +
           "JOIN b.product p " +
           "JOIN p.declaration d " +
           "WHERE d.poNo = :poNo AND p.productType = :railPadType " +
           "AND d.id IN (SELECT v.requestId FROM RailIEProductionVerification v) " +
           "AND b.id NOT IN (SELECT ib.declarationBatchId FROM RailProcessInspectionBatch ib WHERE ib.declarationBatchId IS NOT NULL)")
    List<RailProductionBatch> findAvailableBatches(@Param("poNo") String poNo, @Param("railPadType") String railPadType);
}
