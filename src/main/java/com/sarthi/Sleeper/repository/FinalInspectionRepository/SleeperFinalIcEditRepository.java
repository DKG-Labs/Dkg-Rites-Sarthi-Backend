package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperFinalIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SleeperFinalIcEditRepository extends JpaRepository<SleeperFinalIcEdit, Long> {
    Optional<SleeperFinalIcEdit> findByIcNumber(String icNumber);
}
