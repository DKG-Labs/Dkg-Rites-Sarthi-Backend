package com.sarthi.repository;

import com.sarthi.entity.WorkflowDeleteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowDeleteHistoryRepository extends JpaRepository<WorkflowDeleteHistory, Long> {
}
