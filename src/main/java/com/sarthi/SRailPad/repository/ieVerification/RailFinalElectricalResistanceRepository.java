package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalElectricalResistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalElectricalResistanceRepository extends JpaRepository<RailFinalElectricalResistance, Long> {
    Optional<RailFinalElectricalResistance> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalElectricalResistance> findAllByCallNo(String callNo);
}
