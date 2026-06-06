package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalSpecificGravity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalSpecificGravityRepository extends JpaRepository<RailFinalSpecificGravity, Long> {
    Optional<RailFinalSpecificGravity> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalSpecificGravity> findAllByCallNo(String callNo);
}
