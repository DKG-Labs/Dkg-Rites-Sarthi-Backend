package com.sarthi.repository;

import com.sarthi.entity.FinalIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinalIeMappingRepository extends JpaRepository<FinalIeMapping, Long> {
    List<FinalIeMapping> findByWorkflowTransitionId(Integer workflowTransitionId);

    /**
     * Batch fetch final IE mappings by workflow transition IDs (for performance optimization)
     */
    List<FinalIeMapping> findByWorkflowTransitionIdIn(List<Integer> workflowTransitionIds);

    boolean existsByWorkflowTransitionIdAndIeUserId(Integer workflowTransitionId, Integer actionBy);

    @org.springframework.data.jpa.repository.Modifying
    void deleteByWorkflowTransitionId(Integer workflowTransitionId);
}
