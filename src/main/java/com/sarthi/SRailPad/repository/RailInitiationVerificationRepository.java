package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.inspectionCall.RailInitiationVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RailInitiationVerificationRepository extends JpaRepository<RailInitiationVerification, Long> {

    Optional<RailInitiationVerification> findByCallNo(String callNo);

    boolean existsByCallNo(String callNo);
}
