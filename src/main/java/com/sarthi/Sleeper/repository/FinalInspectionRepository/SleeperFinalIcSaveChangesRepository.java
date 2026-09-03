package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperFinalIcSaveChanges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SleeperFinalIcSaveChangesRepository extends JpaRepository<SleeperFinalIcSaveChanges, Long> {
    Optional<SleeperFinalIcSaveChanges> findByIcNumber(String icNumber);
}
