package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailIEProductionVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailIEProductionVerificationRepository extends JpaRepository<RailIEProductionVerification, Long> {
    Optional<RailIEProductionVerification> findTopByRequestIdOrderByIdDesc(Long requestId);
}
