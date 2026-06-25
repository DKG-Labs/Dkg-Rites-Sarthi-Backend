package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailpadFinalIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailpadFinalIcSaveChangesRepository extends JpaRepository<RailpadFinalIcSaveChanges, Long> {
    Optional<RailpadFinalIcSaveChanges> findByIcNumber(String icNumber);
}
