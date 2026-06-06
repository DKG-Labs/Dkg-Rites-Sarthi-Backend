package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalCompressionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalCompressionSetRepository extends JpaRepository<RailFinalCompressionSet, Long> {
    Optional<RailFinalCompressionSet> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalCompressionSet> findAllByCallNo(String callNo);
}
