package com.sarthi.repository;

import com.sarthi.entity.FinalIeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Check if an IE user is authorized for any transition in the given request.
     * Used by validateFinalIeAction to avoid breaking when a new transition (e.g. DSC_SIGN_IC)
     * is created without copying FinalIeMapping rows from the previous transition.
     */
    @Query("SELECT COUNT(f) > 0 FROM FinalIeMapping f " +
           "JOIN WorkflowTransition wt ON f.workflowTransitionId = wt.workflowTransitionId " +
           "WHERE wt.requestId = :requestId AND f.ieUserId = :ieUserId")
    boolean existsByRequestIdAndIeUserId(
            @Param("requestId") String requestId,
            @Param("ieUserId") Integer ieUserId
    );

    @org.springframework.data.jpa.repository.Modifying
    void deleteByWorkflowTransitionId(Integer workflowTransitionId);
}
