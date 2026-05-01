package com.sarthi.Sleeper.repository.FInalCallRepo;

import com.sarthi.Sleeper.entity.FInalCall.FinalCallInspectionHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinalCallInspectionHeaderRepository extends JpaRepository<FinalCallInspectionHeader, Long> {
    Optional<FinalCallInspectionHeader> findByCallNo(String callNo);
}
