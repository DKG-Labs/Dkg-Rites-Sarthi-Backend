package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalNcrAdhesionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalNcrAdhesionTestRepository extends JpaRepository<RailFinalNcrAdhesionTest, Long> {
    Optional<RailFinalNcrAdhesionTest> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalNcrAdhesionTest> findAllByCallNo(String callNo);
}
