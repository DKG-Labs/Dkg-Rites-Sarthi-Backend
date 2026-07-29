package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalPeriodicAbrasion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailFinalPeriodicAbrasionRepository extends JpaRepository<RailFinalPeriodicAbrasion, Long> {
    Optional<RailFinalPeriodicAbrasion> findByCallNoAndLotNo(String callNo, String lotNo);
}
