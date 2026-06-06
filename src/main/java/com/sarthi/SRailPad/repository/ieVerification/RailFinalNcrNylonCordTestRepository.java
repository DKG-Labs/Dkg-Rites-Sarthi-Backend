package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalNcrNylonCordTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalNcrNylonCordTestRepository extends JpaRepository<RailFinalNcrNylonCordTest, Long> {
    Optional<RailFinalNcrNylonCordTest> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalNcrNylonCordTest> findAllByCallNo(String callNo);
}
