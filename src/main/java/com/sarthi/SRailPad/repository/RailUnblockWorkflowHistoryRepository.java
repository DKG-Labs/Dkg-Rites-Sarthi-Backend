package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.RailUnblockWorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailUnblockWorkflowHistoryRepository extends JpaRepository<RailUnblockWorkflowHistory, Long> {
    List<RailUnblockWorkflowHistory> findAllByModuleId(Long moduleId);
    List<RailUnblockWorkflowHistory> findAllByRequestIdAndModuleId(String requestId, Long moduleId);
    List<RailUnblockWorkflowHistory> findAllByPlantIdOrderByUnblockedOnDesc(String plantId);
}
