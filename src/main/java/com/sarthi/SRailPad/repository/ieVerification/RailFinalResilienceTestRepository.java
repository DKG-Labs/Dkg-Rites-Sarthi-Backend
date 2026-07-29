package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalResilienceTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailFinalResilienceTestRepository extends JpaRepository<RailFinalResilienceTest, Long> {
    Optional<RailFinalResilienceTest> findByCallNoAndLotNo(String callNo, String lotNo);
}
