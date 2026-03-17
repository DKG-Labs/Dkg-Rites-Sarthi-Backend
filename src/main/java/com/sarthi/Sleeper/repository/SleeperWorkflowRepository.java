package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperWorkflowRepository
        extends JpaRepository<SleeperWorkflowTransaction, Long> {

    List<SleeperWorkflowTransaction> findByAssignedToUserAndStatusIn(Long userId, List<String> status);

    List<SleeperWorkflowTransaction> findByRequestIdOrderByCreatedDateAsc(String requestId);

    @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId = (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        WHERE t2.requestId = t.requestId
    )
    AND t.status IN ('CREATED','PENDING')
    AND t.nextRole = :roleName
""")
    List<SleeperWorkflowTransaction> findLastPendingRequestsByRole(String roleName);

    @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId = (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        WHERE t2.requestId = t.requestId
    )
    AND t.status = 'COMPLETED'
""")
    List<SleeperWorkflowTransaction> findLastCompletedRequests();

    @Query("""
SELECT t FROM SleeperWorkflowTransaction t
WHERE t.workflowTransitionId = (
    SELECT MAX(t2.workflowTransitionId)
    FROM SleeperWorkflowTransaction t2
    WHERE t2.requestId = t.requestId
)
AND t.status = 'Completed'
""")
    List<SleeperWorkflowTransaction> findCompletedRequests();
/*
    @Query(value = """
    SELECT status 
    FROM sleeper_workflow_transaction 
    WHERE request_id = :requestId 
    ORDER BY workflow_transition_id DESC 
    LIMIT 1
""", nativeQuery = true)
    Optional<String> findLatestStatusByRequestId(@Param("requestId") String requestId);

 */
@Query(value = """
    SELECT status 
    FROM sleeper_workflow_transaction 
    WHERE request_id = :requestId 
      AND module_id = :moduleId
    ORDER BY workflow_transition_id DESC 
    LIMIT 1
""", nativeQuery = true)
Optional<String> findLatestStatusByRequestIdAndModuleId(
        @Param("requestId") String requestId,
        @Param("moduleId") Long moduleId
);
}