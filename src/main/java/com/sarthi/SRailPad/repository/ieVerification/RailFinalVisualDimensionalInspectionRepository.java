package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalVisualDimensionalInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalVisualDimensionalInspectionRepository extends JpaRepository<RailFinalVisualDimensionalInspection, Long> {
    Optional<RailFinalVisualDimensionalInspection> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalVisualDimensionalInspection> findAllByCallNo(String callNo);
}
