package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalElongation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalElongationRepository extends JpaRepository<RailFinalElongation, Long> {
    Optional<RailFinalElongation> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalElongation> findAllByCallNo(String callNo);
}
