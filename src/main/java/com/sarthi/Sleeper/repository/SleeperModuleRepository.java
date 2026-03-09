package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SleeperModuleRepository extends JpaRepository<SleeperModule, Long> {
    boolean existsByIdAndWorkflowId(Long moduleId, Long workflowId);
}
