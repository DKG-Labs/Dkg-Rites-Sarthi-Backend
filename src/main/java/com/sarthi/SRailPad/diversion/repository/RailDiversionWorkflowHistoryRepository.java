package com.sarthi.SRailPad.diversion.repository;

import com.sarthi.SRailPad.diversion.entity.RailDiversionWorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailDiversionWorkflowHistoryRepository extends JpaRepository<RailDiversionWorkflowHistory, Long> {

    List<RailDiversionWorkflowHistory> findByDiversionRequestIdOrderByCreatedDateAsc(Long requestId);
}
