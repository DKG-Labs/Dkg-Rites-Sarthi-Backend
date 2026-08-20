package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalOzoneTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailFinalOzoneTestRepository extends JpaRepository<RailFinalOzoneTest, Long> {
    Optional<RailFinalOzoneTest> findByCallNoAndLotNo(String callNo, String lotNo);
}
