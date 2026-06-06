package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalHardnessTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalHardnessTestRepository extends JpaRepository<RailFinalHardnessTest, Long> {
    Optional<RailFinalHardnessTest> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalHardnessTest> findAllByCallNo(String callNo);
}
