package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailProcessInspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailProcessInspectionResultRepository extends JpaRepository<RailProcessInspectionResult, Long> {
    Optional<RailProcessInspectionResult> findByInspectionCall_CallNo(String callNo);
}
