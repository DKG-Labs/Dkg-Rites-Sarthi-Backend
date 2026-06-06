package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalAshContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailFinalAshContentRepository extends JpaRepository<RailFinalAshContent, Long> {
    Optional<RailFinalAshContent> findByCallNoAndLotNo(String callNo, String lotNo);
    List<RailFinalAshContent> findAllByCallNo(String callNo);
}
