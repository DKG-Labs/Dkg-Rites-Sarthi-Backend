package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalModulus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalModulusRepository extends JpaRepository<RailFinalModulus, Long> {
    Optional<RailFinalModulus> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalModulus> findAllByCallNo(String callNo);
}
