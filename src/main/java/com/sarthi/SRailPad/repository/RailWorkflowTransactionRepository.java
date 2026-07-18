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
    AND COALESCE(t2.moduleId, 0) = COALESCE(t.moduleId, 0)
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
    AND COALESCE(t2.moduleId, 0) = COALESCE(t.moduleId, 0)
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
    AND COALESCE(t2.moduleId, 0) = COALESCE(t.moduleId, 0)
)
AND t.action IN :pendingActions
AND UPPER(t.status) = 'PENDING'
""")
    List<RailWorkflowTransaction> findPendingVerifiedCalls(@Param("pendingActions") List<String> pendingActions);

    @Query("""
SELECT t FROM RailWorkflowTransaction t
WHERE t.workflowTransitionId = (
    SELECT MAX(t2.workflowTransitionId)
    FROM RailWorkflowTransaction t2
    WHERE t2.requestId = t.requestId
    AND COALESCE(t2.moduleId, 0) = COALESCE(t.moduleId, 0)
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
    AND (t2.moduleId = t.moduleId OR (t2.moduleId IS NULL AND t.moduleId IS NULL))
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

    @Query(value = """
                SELECT status 
                FROM rail_workflow_transaction 
                WHERE request_id = :requestId 
                  AND module_id = :moduleId
                  AND plant_id = :plantId
                ORDER BY workflow_transition_id DESC 
                LIMIT 1
            """, nativeQuery = true)
    Optional<String> findLatestStatusByRequestIdAndModuleIdAndPlantId(
            @Param("requestId") String requestId,
            @Param("moduleId") Long moduleId,
            @Param("plantId") String plantId
    );

    @Query(value = "SELECT poi_code FROM rail_workflow_transaction WHERE request_id = :requestId ORDER BY workflow_transition_id DESC LIMIT 1", nativeQuery = true)
    String findLatestPoiByRequestId(@Param("requestId") String requestId);

    @Query(value = "SELECT status FROM rail_workflow_transaction WHERE request_id = :requestId ORDER BY workflow_transition_id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestStatusByRequestId(@Param("requestId") String requestId);

    @Query(value = """
        SELECT 
            COALESCE(SUM(CASE WHEN t.has_initiate = 1 THEN 1 ELSE 0 END), 0) as under_inspection,
            COALESCE(SUM(CASE WHEN t.has_initiate = 0 THEN 1 ELSE 0 END), 0) as pending
        FROM (
            SELECT 
                rwt1.request_id,
                CASE WHEN EXISTS (
                    SELECT 1 
                    FROM rail_workflow_transaction rwt2 
                    WHERE rwt2.request_id = rwt1.request_id
                      AND rwt2.workflow_id = 2
                      AND (UPPER(rwt2.action) = 'INITIATE_CALL' OR UPPER(rwt2.job_status) = 'INITIATED')
                ) THEN 1 ELSE 0 END as has_initiate
            FROM rail_workflow_transaction rwt1
            WHERE rwt1.workflow_id = 2
              AND rwt1.workflow_transition_id IN (
                  SELECT MAX(rwt3.workflow_transition_id) 
                  FROM rail_workflow_transaction rwt3
                  WHERE rwt3.workflow_id = 2
                  GROUP BY rwt3.request_id
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM rail_workflow_transaction rwt4
                  WHERE rwt4.request_id = rwt1.request_id
                    AND rwt4.workflow_id = 2
                    AND (UPPER(rwt4.status) = 'COMPLETED' OR UPPER(rwt4.job_status) = 'COMPLETED')
              )
        ) t
    """, nativeQuery = true)
    List<Object[]> getRailPadInspectionCallCounts();

    @Query(value = """
        SELECT 
            ic.call_no AS inspectionCallNumber,
            COALESCE(vm.vendor_name, ic.vendor_code) AS vendor,
            DATE_FORMAT(ic.created_at, '%d/%m/%Y %H:%i:%s') AS callSubmissionDateTime,
            'Railpad' AS stageOfInspection,
            CONCAT(COALESCE(ph.rly_cd, 'N/A'), ' / ', ic.po_no) AS poSrNo,
            DATE_FORMAT(pi.delivery_date, '%d/%m/%Y') AS dpDate,
            CASE 
                WHEN t.has_initiate = 1 THEN 'Under Inspection'
                ELSE 'Pending'
            END AS status
        FROM (
            SELECT 
                rwt1.request_id,
                CASE WHEN EXISTS (
                    SELECT 1 
                    FROM rail_workflow_transaction rwt2 
                    WHERE rwt2.request_id = rwt1.request_id
                      AND rwt2.workflow_id = 2
                      AND (UPPER(rwt2.action) = 'INITIATE_CALL' OR UPPER(rwt2.job_status) = 'INITIATED')
                ) THEN 1 ELSE 0 END as has_initiate
            FROM rail_workflow_transaction rwt1
            WHERE rwt1.workflow_id = 2
              AND rwt1.workflow_transition_id IN (
                  SELECT MAX(rwt3.workflow_transition_id) 
                  FROM rail_workflow_transaction rwt3
                  WHERE rwt3.workflow_id = 2
                  GROUP BY rwt3.request_id
              )
              AND NOT EXISTS (
                  SELECT 1
                  FROM rail_workflow_transaction rwt4
                  WHERE rwt4.request_id = rwt1.request_id
                    AND rwt4.workflow_id = 2
                    AND (UPPER(rwt4.status) = 'COMPLETED' OR UPPER(rwt4.job_status) = 'COMPLETED')
              )
        ) t
        INNER JOIN rail_inspection_call ic ON t.request_id = ic.call_no
        LEFT JOIN vendor_master vm ON vm.vendor_code = ic.vendor_code
        LEFT JOIN po_header ph ON ph.po_no = SUBSTRING_INDEX(ic.po_no, '/', 1)
        LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND pi.item_sr_no = SUBSTRING_INDEX(ic.po_no, '/', -1)
        WHERE 
            (:status = 'ALL' OR 
             (:status = 'Under Inspection' AND t.has_initiate = 1) OR
             (:status = 'Pending' AND t.has_initiate = 0))
        ORDER BY ic.created_at DESC
    """, nativeQuery = true)
    List<Object[]> getRailPadInspectionCallStatusDetailsRaw(@Param("status") String status);
}
