package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalPeriodicTga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailFinalPeriodicTgaRepository extends JpaRepository<RailFinalPeriodicTga, Long> {
    Optional<RailFinalPeriodicTga> findByCallNoAndLotNo(String callNo, String lotNo);
}
