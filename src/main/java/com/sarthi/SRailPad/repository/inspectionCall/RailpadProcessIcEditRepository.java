package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailpadProcessIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RailpadProcessIcEditRepository extends JpaRepository<RailpadProcessIcEdit, Long> {
    Optional<RailpadProcessIcEdit> findByIcNumber(String icNumber);
}
