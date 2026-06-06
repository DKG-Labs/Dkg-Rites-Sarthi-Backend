package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalSecantStiffnessTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalSecantStiffnessTestRepository extends JpaRepository<RailFinalSecantStiffnessTest, Long> {
    Optional<RailFinalSecantStiffnessTest> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalSecantStiffnessTest> findAllByCallNo(String callNo);
}
