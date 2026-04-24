package com.sarthi.repository;

import com.sarthi.entity.WorkflowTransition;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Integer> {
  WorkflowTransition findByWorkflowIdAndRequestId(Integer workflowId, String requestId);

  @Query(value = "SELECT * FROM WORKFLOW_TRANSITION WHERE WORKFLOWTRANSITIONID IN (" +
          "  SELECT MAX(WORKFLOWTRANSITIONID) " +
          "  FROM WORKFLOW_TRANSITION " +
          "  WHERE (:rio IS NULL OR RIO = :rio) " +
          "  GROUP BY REQUESTID" +
          ")", nativeQuery = true)
  List<WorkflowTransition> findLatestByRio(@Param("rio") String rio);

  //    @Query("SELECT COUNT(w) FROM WorkflowTransition w " +
//            "WHERE w.assignedToUser = :ieUserId AND w.status IN ('VERIFIED','ASSIGNED','INITIATED')")
//    int countActiveCallsForIE(@Param("ieUserId") Integer ieUserId);
  @Query("SELECT COUNT(w) FROM WorkflowTransition w " +
          "WHERE w.workflowTransitionId IN (" +
          "   SELECT MAX(w2.workflowTransitionId) FROM WorkflowTransition w2 " +
          "   WHERE w2.assignedToUser = :ieUserId GROUP BY w2.requestId" +
          ") AND w.jobStatus IN ('ASSIGNED','IN_PROGRESS','VERIFIED','INITIATED')")
  int countActiveCallsForIE(@Param("ieUserId") Integer ieUserId);


  WorkflowTransition findTopByRequestIdOrderByWorkflowTransitionIdDesc(String requestId);

  @Query(value = "SELECT * FROM WORKFLOW_TRANSITION WHERE WORKFLOWTRANSITIONID IN (" +
          "  SELECT MAX(WORKFLOWTRANSITIONID) " +
          "  FROM WORKFLOW_TRANSITION " +
          "  WHERE REQUESTID IN (:requestIds) " +
          "  GROUP BY REQUESTID" +
          ")", nativeQuery = true)
  List<WorkflowTransition> findLatestByRequestIds(@Param("requestIds") List<String> requestIds);
  /*
      @Query("SELECT wt FROM WorkflowTransition wt " +
              "WHERE wt.workflowTransitionId IN (" +
              "    SELECT MAX(wt2.workflowTransitionId) " +
              "    FROM WorkflowTransition wt2 " +
              "    GROUP BY wt2.requestId" +
              ") " +
              "AND wt.nextRoleName = :roleName " +
              "AND wt.jobStatus IN ('IN_PROGRESS','VERIFIED','APPROVED','REGISTERED','Created','ASSIGNED','REJECTED','PAUSED')")
      List<WorkflowTransition> findPendingByRole(@Param("roleName") String roleName);

      */
  @Query("SELECT wt FROM WorkflowTransition wt " +
          "WHERE wt.workflowTransitionId IN (" +
          "   SELECT MAX(wt2.workflowTransitionId) " +
          "   FROM WorkflowTransition wt2 " +
          "   WHERE wt2.transitionId NOT IN (42,44,45) " +   // Exclude qty edit transitions
          "   GROUP BY wt2.requestId" +
          ") " +
          "AND wt.nextRoleName = :roleName " +
          "AND wt.jobStatus IN ('IN_PROGRESS','VERIFIED','APPROVED','REGISTERED','Created','ASSIGNED','REJECTED','PAUSED')")
  List<WorkflowTransition> findPendingByRole(@Param("roleName") String roleName);

  @Query("""
SELECT wt FROM WorkflowTransition wt
WHERE wt.workflowTransitionId IN (
    SELECT MAX(wt2.workflowTransitionId)
    FROM WorkflowTransition wt2
    WHERE wt2.transitionId NOT IN (42,44,45)
    GROUP BY wt2.requestId
)
AND wt.nextRoleName IN :roleNames
AND wt.jobStatus IN (
    'IN_PROGRESS','VERIFIED','APPROVED','REGISTERED',
    'Created','ASSIGNED','REJECTED','PAUSED'
)
""")
  List<WorkflowTransition> findPendingByRoles(
          @Param("roleNames") List<String> roleNames
  );



  @Query("SELECT wt FROM WorkflowTransition wt " +
          "WHERE wt.workflowTransitionId IN (" +
          "     SELECT MAX(wt2.workflowTransitionId) " +
          "     FROM WorkflowTransition wt2 " +
          "     WHERE wt2.transitionId IN (42, 44, 45) " +
          "     GROUP BY wt2.requestId" +
          ") " +
          "AND wt.nextRoleName = :roleName " +
          "ORDER BY wt.createdDate DESC")
  List<WorkflowTransition> findPendingQtyEditByRole(@Param("roleName") String roleName);

  List<WorkflowTransition> findByRequestId(String requestId);

  @Query("SELECT w FROM WorkflowTransition w " +
          "WHERE w.status = :status " +
          "AND w.requestId = :requestId " +
          "AND w.currentRoleName = :roleName")
  WorkflowTransition findByStatusRequestIdAndCurrentRoleName(
          @Param("status") String status,
          @Param("requestId") String requestId,
          @Param("roleName") String roleName
  );

  WorkflowTransition findTopByRequestIdAndStatus(String requestId, String initiateInspection);

  WorkflowTransition findTopByRequestIdAndStatusOrderByWorkflowTransitionIdDesc(String requestId, String callRegistered);

  @Query("SELECT w FROM WorkflowTransition w WHERE w.status = 'BLOCKED'")
  List<WorkflowTransition> findBlockedTransitions();

  List<WorkflowTransition> findAllByStatusAndCreatedBy(String status, Integer createdBy);

  @Query("""
SELECT DISTINCT wt
FROM WorkflowTransition wt
LEFT JOIN ProcessIeUsers pm
       ON wt.processIeUserId = pm.processUserId
WHERE wt.workflowTransitionId IN (
    SELECT MAX(wt2.workflowTransitionId)
    FROM WorkflowTransition wt2
    GROUP BY wt2.requestId
)
AND wt.status IN ('INSPECTION_COMPLETE_CONFIRM', 'GENERATE_IC', 'DSC_SIGN_IC')
  AND (
       (wt.requestId LIKE 'EP%' AND
           (pm.ieUserId = :userId
            OR wt.processIeUserId = :userId
            OR wt.createdBy = :userId)
       )
       OR
       (wt.requestId NOT LIKE 'EP%'
            AND wt.createdBy = :userId
       )
  )
""")
  List<WorkflowTransition> findCompletedByUserRule(
          @Param("userId") Long userId
  );




  @Query("""
SELECT DISTINCT wt
FROM WorkflowTransition wt
LEFT JOIN ProcessIeUsers pm
       ON wt.processIeUserId = pm.processUserId
WHERE wt.workflowTransitionId IN (
    SELECT MAX(wt2.workflowTransitionId)
    FROM WorkflowTransition wt2
    GROUP BY wt2.requestId
)
AND wt.status = 'DSC_SIGN_IC'
  AND (
       (wt.requestId LIKE 'EP%' AND
           (pm.ieUserId = :userId
            OR wt.processIeUserId = :userId
            OR wt.createdBy = :userId)
       )
       OR
       (wt.requestId NOT LIKE 'EP%'
            AND wt.createdBy = :userId
       )
  )
""")
  List<WorkflowTransition> findSignedByUserRule(
          @Param("userId") Long userId
  );

  @Query("""
    SELECT
        MIN(w.createdDate),
        MAX(
            CASE
                WHEN w.status = 'INSPECTION_COMPLETE_CONFIRM'
                THEN w.createdDate
                ELSE NULL
            END
        )
    FROM WorkflowTransition w
    WHERE w.requestId = :requestId
""")
  List<Object[]> findStartAndEndDateByRequestId(
          @Param("requestId") String requestId);


  @Query("""
    SELECT
        w.requestId,
        MIN(w.createdDate),
        MAX(
            CASE
                WHEN w.status = 'INSPECTION_COMPLETE_CONFIRM'
                THEN w.createdDate
            END
        )
    FROM WorkflowTransition w
    WHERE w.requestId IN :requestIds
    GROUP BY w.requestId
""")
  List<Object[]> findStartAndEndDateByRequestIds(
          @Param("requestIds") List<String> requestIds
  );

  @Query(value = """
        SELECT 'RM' as category,
               COUNT(DISTINCT CASE WHEN has_initiate = 1 AND is_complete = 0 THEN requestId END) as under,
               COUNT(DISTINCT CASE WHEN has_created = 1 AND has_initiate = 0 AND is_complete = 0 THEN requestId END) as pending
        FROM (
            SELECT requestId,
                   MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END) as has_initiate,
                   MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) as has_created,
                   MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) as is_complete
            FROM workflow_transition
            WHERE requestId LIKE '%ER%'
            GROUP BY requestId
        ) t1
        UNION ALL
        SELECT 'Process' as category,
               COUNT(DISTINCT CASE WHEN has_initiate = 1 AND is_complete = 0 THEN requestId END) as under,
               COUNT(DISTINCT CASE WHEN has_created = 1 AND has_initiate = 0 AND is_complete = 0 THEN requestId END) as pending
        FROM (
            SELECT requestId,
                   MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END) as has_initiate,
                   MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) as has_created,
                   MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) as is_complete
            FROM workflow_transition
            WHERE requestId LIKE '%EP%'
            GROUP BY requestId
        ) t2
        UNION ALL
        SELECT 'Final' as category,
               COUNT(DISTINCT CASE WHEN has_initiate = 1 AND is_complete = 0 THEN requestId END) as under,
               COUNT(DISTINCT CASE WHEN has_created = 1 AND has_initiate = 0 AND is_complete = 0 THEN requestId END) as pending
        FROM (
            SELECT requestId,
                   MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END) as has_initiate,
                   MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) as has_created,
                   MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) as is_complete
            FROM workflow_transition
            WHERE requestId LIKE '%EF%'
            GROUP BY requestId
        ) t3
        """, nativeQuery = true)
  List<Object[]> getInspectionCallStatusBreakdown();

  @Query(value = """
        SELECT 'RM' as category,
               COUNT(DISTINCT CASE WHEN has_initiate = 1 AND is_complete = 0 THEN requestId END) as under,
               COUNT(DISTINCT CASE WHEN has_created = 1 AND has_initiate = 0 AND is_complete = 0 THEN requestId END) as pending
        FROM (
            SELECT requestId,
                   MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END) as has_initiate,
                   MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) as has_created,
                   MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) as is_complete
            FROM workflow_transition
            WHERE requestId LIKE '%ER%'
              AND requestId NOT IN (SELECT ic_number FROM inspection_calls WHERE po_no = :excludePo)
            GROUP BY requestId
        ) t1
        UNION ALL
        SELECT 'Process' as category,
               COUNT(DISTINCT CASE WHEN has_initiate = 1 AND is_complete = 0 THEN requestId END) as under,
               COUNT(DISTINCT CASE WHEN has_created = 1 AND has_initiate = 0 AND is_complete = 0 THEN requestId END) as pending
        FROM (
            SELECT requestId,
                   MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END) as has_initiate,
                   MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) as has_created,
                   MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) as is_complete
            FROM workflow_transition
            WHERE requestId LIKE '%EP%'
               AND requestId NOT IN (SELECT ic_number FROM inspection_calls WHERE po_no = :excludePo)
            GROUP BY requestId
        ) t2
        UNION ALL
        SELECT 'Final' as category,
               COUNT(DISTINCT CASE WHEN has_initiate = 1 AND is_complete = 0 THEN requestId END) as under,
               COUNT(DISTINCT CASE WHEN has_created = 1 AND has_initiate = 0 AND is_complete = 0 THEN requestId END) as pending
        FROM (
            SELECT requestId,
                   MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END) as has_initiate,
                   MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) as has_created,
                   MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) as is_complete
            FROM workflow_transition
            WHERE requestId LIKE '%EF%'
               AND requestId NOT IN (SELECT ic_number FROM inspection_calls WHERE po_no = :excludePo)
            GROUP BY requestId
        ) t3
        """, nativeQuery = true)
  List<Object[]> getInspectionCallStatusBreakdownExcludingDummyPo(@Param("excludePo") String excludePo);
}