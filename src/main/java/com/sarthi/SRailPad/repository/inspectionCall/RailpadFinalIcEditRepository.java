package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailpadFinalIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailpadFinalIcEditRepository extends JpaRepository<RailpadFinalIcEdit, Long> {
    Optional<RailpadFinalIcEdit> findByIcNumber(String icNumber);
}
