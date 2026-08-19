package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailWithdrawnFinalCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailWithdrawnFinalCallRepository extends JpaRepository<RailWithdrawnFinalCall, Long> {
    Optional<RailWithdrawnFinalCall> findByCallNo(String callNo);
}
