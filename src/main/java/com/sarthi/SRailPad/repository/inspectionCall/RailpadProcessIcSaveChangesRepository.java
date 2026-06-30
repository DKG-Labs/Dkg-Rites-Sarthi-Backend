package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailpadProcessIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RailpadProcessIcSaveChangesRepository extends JpaRepository<RailpadProcessIcSaveChanges, Long> {
    Optional<RailpadProcessIcSaveChanges> findByIcNumber(String icNumber);
}
