package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalLoadTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalLoadTestRepository extends JpaRepository<RailFinalLoadTest, Long> {
    Optional<RailFinalLoadTest> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalLoadTest> findAllByCallNo(String callNo);
}
