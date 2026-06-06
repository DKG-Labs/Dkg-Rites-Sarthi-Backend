package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalTensionSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalTensionSetRepository extends JpaRepository<RailFinalTensionSet, Long> {
    Optional<RailFinalTensionSet> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalTensionSet> findAllByCallNo(String callNo);
}
