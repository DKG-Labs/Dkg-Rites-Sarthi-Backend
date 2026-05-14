package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailInspectionCallRepository extends JpaRepository<RailInspectionCall, Long> {
    
    @Query(value = "SELECT call_no FROM rail_inspection_call WHERE call_no LIKE ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastCallNoByPattern(String pattern);

    @Query("SELECT SUM(c.totalQty) FROM RailInspectionCall c WHERE c.poNo = :poNo")
    Integer findTotalQtyByPoNo(String poNo);

    List<RailInspectionCall> findAllByVendorCode(String vendorCode);

    Optional<RailInspectionCall> findByCallNo(String callNo);
}
