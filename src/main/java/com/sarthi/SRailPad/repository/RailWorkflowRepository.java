package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.RailWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RailWorkflowRepository extends JpaRepository<RailWorkflow, Long> {
}
