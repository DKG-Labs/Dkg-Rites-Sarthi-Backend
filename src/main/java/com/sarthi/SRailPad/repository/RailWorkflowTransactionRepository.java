package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.RailWorkflowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.List;

@Repository
public interface RailWorkflowTransactionRepository extends JpaRepository<RailWorkflowTransaction, Integer> {
    @Query("""
SELECT t FROM RailWorkflowTransaction t
WHERE t.workflowTransitionId = (
    SELECT MAX(t2.workflowTransitionId)
    FROM RailWorkflowTransaction t2
    WHERE t2.requestId = t.requestId
)
AND UPPER(t.status) IN ('CREATED','PENDING', 'CREATE', 'RETURNED')
AND t.nextRole = :roleName
""")
    List<RailWorkflowTransaction> findLatestByRole(String roleName);

    @Query("""
SELECT t FROM RailWorkflowTransaction t
WHERE t.workflowTransitionId = (
    SELECT MAX(t2.workflowTransitionId)
    FROM RailWorkflowTransaction t2
    WHERE t2.requestId = t.requestId
)
AND UPPER(t.status) IN ('CREATED','PENDING', 'CREATE', 'RETURNED', 'RESUBMITTED')
AND t.nextRole = :roleName
""")
    List<RailWorkflowTransaction> findLastPendingRequestsByRole(String roleName);

    List<RailWorkflowTransaction> findByRequestIdOrderByCreatedDateAsc(String requestId);

    @Query("""
SELECT t FROM RailWorkflowTransaction t
WHERE t.workflowTransitionId = (
    SELECT MAX(t2.workflowTransitionId)
    FROM RailWorkflowTransaction t2
    WHERE t2.requestId = t.requestId
)
AND UPPER(t.status) = 'COMPLETED'
""")
    List<RailWorkflowTransaction> findCompletedRequests();

    @Query("""
SELECT t FROM RailWorkflowTransaction t
WHERE t.workflowTransitionId = (
    SELECT MAX(t2.workflowTransitionId)
    FROM RailWorkflowTransaction t2
    WHERE t2.requestId = t.requestId
    AND t2.workflowId = 2
    AND UPPER(t2.status) = 'COMPLETED'
)
AND UPPER(t.status) = 'COMPLETED'
AND t.workflowId = 2
""")
    List<RailWorkflowTransaction> findFinalCompletedRequests();

    @Query(value = """
                SELECT status 
                FROM rail_workflow_transaction 
                WHERE request_id = :requestId 
                  AND module_id = :moduleId
                ORDER BY workflow_transition_id DESC 
                LIMIT 1
            """, nativeQuery = true)
    Optional<String> findLatestStatusByRequestIdAndModuleId(
            @Param("requestId") String requestId,
            @Param("moduleId") Long moduleId
    );

    @Query(value = "SELECT poi_code FROM rail_workflow_transaction WHERE request_id = :requestId ORDER BY workflow_transition_id DESC LIMIT 1", nativeQuery = true)
    String findLatestPoiByRequestId(@Param("requestId") String requestId);
}
