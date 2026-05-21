package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperWorkflowRepository
        extends JpaRepository<SleeperWorkflowTransaction, Long> {

    List<SleeperWorkflowTransaction> findByAssignedToUserAndStatusIn(Long userId, List<String> status);

    List<SleeperWorkflowTransaction> findByRequestIdOrderByCreatedDateAsc(String requestId);

   /* @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId = (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        WHERE t2.requestId = t.requestId
        AND t2.status IN ('Created','PENDING')
    )
    AND t.nextRole = :roleName
""")
    List<SleeperWorkflowTransaction> findLastPendingRequestsByRole(String roleName); */
  @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId = (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        WHERE t2.requestId = t.requestId
        AND (t2.moduleId = t.moduleId OR (t2.moduleId IS NULL AND t.moduleId IS NULL))
    )
    AND t.status IN ('Created','PENDING')
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
AND UPPER(t.status) IN ('CREATED','PENDING')
AND t.nextRole = :roleName
""")
    List<SleeperWorkflowTransaction> findLatestByRole(String roleName);

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

   /* @Query("""
            SELECT t FROM SleeperWorkflowTransaction t
            WHERE t.workflowTransitionId = (
                SELECT MAX(t2.workflowTransitionId)
                FROM SleeperWorkflowTransaction t2
                WHERE t2.requestId = t.requestId
            )
            AND t.status = 'Completed'
            """)
    List<SleeperWorkflowTransaction> findCompletedRequests();
*/
   @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId = (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        WHERE t2.requestId = t.requestId
        AND t2.moduleId = t.moduleId   
    )
    AND t.status = 'Completed'
""")
   List<SleeperWorkflowTransaction> findCompletedRequests();

    @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId = (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        WHERE t2.requestId = t.requestId
        AND t2.workflowId = 2
        AND t2.status = 'Completed'
    )
    AND t.status = 'Completed'
    AND t.workflowId = 2
""")
    List<SleeperWorkflowTransaction> findFinalCompletedRequests();

    @Query("""
SELECT t.requestId FROM SleeperWorkflowTransaction t
WHERE t.workflowTransitionId = (
    SELECT MAX(t2.workflowTransitionId)
    FROM SleeperWorkflowTransaction t2
    WHERE t2.requestId = t.requestId
)
AND t.moduleId = :moduleId
AND t.status = 'Completed'
""")
    List<String> findCompletedRequestIdsByModuleId(@Param("moduleId") Long moduleId);
/*
    @Query(value = """
    SELECT status 
    FROM sleeper_workflow_transaction 
    WHERE request_id = :requestId 
    ORDER BY workflow_transition_id DESC 
    LIMIT 1
""", nativeQuery = true)
    Optional<String> findLatestStatusByRequestId(@Param("requestId") String requestId);
=======
>>>>>>> sleeper-uday

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

    @Query(value = """
                SELECT EXISTS (
                    SELECT 1
                    FROM sleeper_workflow_transaction swt
                    WHERE swt.request_id = :requestId
                      AND swt.module_id = 11
                      AND swt.status = 'COMPLETED'
                    ORDER BY swt.workflow_transition_id DESC
                    LIMIT 1
                )
            """, nativeQuery = true)
    Long isWorkflowCompleted(@Param("requestId") Long requestId);

    @Query("SELECT s FROM SleeperWorkflowTransaction s " +
            "WHERE s.moduleId = :moduleId AND s.requestId = :requestId " +
            "ORDER BY s.workflowTransitionId DESC LIMIT 1")
    SleeperWorkflowTransaction findTopByModuleIdAndRequestIdOrderByWorkflowTransitionIdDesc(
            @Param("moduleId") Long moduleId,
            @Param("requestId") String requestId);

    @Query("""
SELECT s.requestId, s.status
FROM SleeperWorkflowTransaction s
WHERE s.moduleId = :moduleId
AND s.id IN (
    SELECT MAX(s2.id)
    FROM SleeperWorkflowTransaction s2
    WHERE s2.moduleId = :moduleId
    GROUP BY s2.requestId
)
""")
    List<Object[]> findAllStatusesByModuleId(@Param("moduleId") Long moduleId);

    @Query("""
SELECT s.requestId, s.status
FROM SleeperWorkflowTransaction s
WHERE s.moduleId = :moduleId
AND s.id IN (
    SELECT MAX(s2.id)
    FROM SleeperWorkflowTransaction s2
    WHERE s2.moduleId = :moduleId
    GROUP BY s2.requestId
)
""")
    List<Object[]> findAllLatestStatuses(@Param("moduleId") Long moduleId);

    @Query("""
SELECT t FROM SleeperWorkflowTransaction t
WHERE t.requestId = :plantId
AND t.moduleId = 1
AND t.workflowTransitionId = (
    SELECT MAX(t2.workflowTransitionId)
    FROM SleeperWorkflowTransaction t2
    WHERE t2.requestId = :plantId
    AND t2.moduleId = 1
)
""")
    Optional<SleeperWorkflowTransaction> findLastRecordByPlantId(String plantId);
}