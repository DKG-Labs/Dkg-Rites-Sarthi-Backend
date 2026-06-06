package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalTensileStrength;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalTensileStrengthRepository extends JpaRepository<RailFinalTensileStrength, Long> {
    Optional<RailFinalTensileStrength> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalTensileStrength> findAllByCallNo(String callNo);
}
