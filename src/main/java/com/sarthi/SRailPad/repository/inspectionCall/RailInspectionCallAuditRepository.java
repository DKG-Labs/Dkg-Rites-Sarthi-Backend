package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCallAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailInspectionCallAuditRepository extends JpaRepository<RailInspectionCallAudit, Long> {
    List<RailInspectionCallAudit> findByCallNoOrderByCreatedAtDesc(String callNo);
}
