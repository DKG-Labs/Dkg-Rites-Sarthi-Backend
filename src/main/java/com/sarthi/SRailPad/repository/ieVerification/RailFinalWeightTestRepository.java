package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalWeightTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalWeightTestRepository extends JpaRepository<RailFinalWeightTest, Long> {
    Optional<RailFinalWeightTest> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalWeightTest> findAllByCallNo(String callNo);
}
