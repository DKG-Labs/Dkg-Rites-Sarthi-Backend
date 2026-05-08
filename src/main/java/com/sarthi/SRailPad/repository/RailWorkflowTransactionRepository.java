package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.RailWorkflowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RailWorkflowTransactionRepository extends JpaRepository<RailWorkflowTransaction, Integer> {
}
