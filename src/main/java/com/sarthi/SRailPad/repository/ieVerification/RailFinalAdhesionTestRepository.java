package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalAdhesionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalAdhesionTestRepository extends JpaRepository<RailFinalAdhesionTest, Long> {
    Optional<RailFinalAdhesionTest> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalAdhesionTest> findAllByCallNo(String callNo);
}
