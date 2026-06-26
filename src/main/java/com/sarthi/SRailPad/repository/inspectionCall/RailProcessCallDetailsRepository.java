package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailProcessCallDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailProcessCallDetailsRepository extends JpaRepository<RailProcessCallDetails, Long> {
    Optional<RailProcessCallDetails> findByInspectionCall_CallNo(String callNo);
}
