package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalNcrBreakingLoadTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalNcrBreakingLoadTestRepository extends JpaRepository<RailFinalNcrBreakingLoadTest, Long> {
    Optional<RailFinalNcrBreakingLoadTest> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalNcrBreakingLoadTest> findAllByCallNo(String callNo);
}
