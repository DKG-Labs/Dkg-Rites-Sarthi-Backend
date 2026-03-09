package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SleeperWorkflowMasterRepository extends JpaRepository<SleeperWorkflow, Long> {
}
