package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalPeriodicDurability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailFinalPeriodicDurabilityRepository extends JpaRepository<RailFinalPeriodicDurability, Long> {
    Optional<RailFinalPeriodicDurability> findByCallNoAndLotNo(String callNo, String lotNo);
}
