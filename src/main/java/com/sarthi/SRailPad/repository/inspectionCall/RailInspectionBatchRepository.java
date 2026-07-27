package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface RailInspectionBatchRepository extends JpaRepository<RailInspectionBatch, Long> {

    @Query("SELECT SUM(b.quantity) FROM RailInspectionBatch b WHERE b.batchNo = :batchNo AND b.productionDate = :productionDate")
    Integer findTotalOfferedByBatchAndDate(@Param("batchNo") String batchNo, @Param("productionDate") LocalDate productionDate);

    @Query("SELECT COALESCE(SUM(COALESCE(b.qtyToUse, b.quantity)), 0) FROM RailInspectionBatch b " +
           "WHERE b.batchNo = :batchNo " +
           "AND (:drawingNo IS NULL OR :drawingNo = '' OR REPLACE(b.drawingNo, 'RDSO/', '') = REPLACE(:drawingNo, 'RDSO/', ''))")
    Integer findTotalOfferedByBatchAndDrawing(@Param("batchNo") String batchNo, @Param("drawingNo") String drawingNo);

    @Query("SELECT COALESCE(SUM(COALESCE(b.qtyToUse, b.quantity)), 0) FROM RailInspectionBatch b " +
           "JOIN b.lot l JOIN l.inspectionCall c " +
           "WHERE b.batchNo = :batchNo " +
           "AND (:drawingNo IS NULL OR :drawingNo = '' OR REPLACE(b.drawingNo, 'RDSO/', '') = REPLACE(:drawingNo, 'RDSO/', '')) " +
           "AND (:processIcNo IS NULL OR c.processIcNo LIKE CONCAT('%', :processIcNo, '%'))")
    Integer findTotalOfferedByBatchDrawingAndProcessIc(
            @Param("batchNo") String batchNo, 
            @Param("drawingNo") String drawingNo, 
            @Param("processIcNo") String processIcNo);

    @Query("SELECT COALESCE(SUM(COALESCE(b.qtyToUse, b.quantity)), 0) FROM RailInspectionBatch b WHERE b.batchNo = :batchNo")
    Integer findTotalOfferedByBatchNo(@Param("batchNo") String batchNo);

    @Query("SELECT b.batchNo, b.drawingNo, COALESCE(SUM(COALESCE(b.qtyToUse, b.quantity)), 0) " +
           "FROM RailInspectionBatch b " +
           "WHERE b.batchNo IN :batchNos " +
           "GROUP BY b.batchNo, b.drawingNo")
    java.util.List<Object[]> findOfferedSummaryByBatchNos(@Param("batchNos") java.util.List<String> batchNos);
}
