package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SleeperWorkflowRepository
        extends JpaRepository<SleeperWorkflowTransaction, Long> {

    List<SleeperWorkflowTransaction> findByAssignedToUserAndStatusIn(Long userId, List<String> status);

    List<SleeperWorkflowTransaction> findByRequestIdOrderByCreatedDateAsc(String requestId);
}