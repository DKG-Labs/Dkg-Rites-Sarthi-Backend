package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCompleteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailInspectionCompleteDetailsRepository extends JpaRepository<RailInspectionCompleteDetails, Long> {
    Optional<RailInspectionCompleteDetails> findByCallNo(String callNo);
}
