package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RailInspectionBatchRepository extends JpaRepository<RailInspectionBatch, Long> {

    @Query("SELECT SUM(b.quantity) FROM RailInspectionBatch b WHERE b.batchNo = :batchNo AND b.productionDate = :productionDate")
    Integer findTotalOfferedByBatchAndDate(String batchNo, LocalDate productionDate);
}
