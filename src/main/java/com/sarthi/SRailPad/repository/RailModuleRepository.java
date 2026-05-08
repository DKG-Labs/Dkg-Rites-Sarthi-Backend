package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.RailModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RailModuleRepository extends JpaRepository<RailModule, Long> {
    boolean existsByIdAndWorkflowId(Long moduleId, Long workflowId);
}
