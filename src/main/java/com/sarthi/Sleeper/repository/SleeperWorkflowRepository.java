package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperWorkflowTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    WHERE t.workflowTransitionId IN (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        GROUP BY t2.requestId, COALESCE(t2.moduleId, 0)
    )
    AND (t.moduleId = t.moduleId OR (t.moduleId IS NULL AND t.moduleId IS NULL))
    AND UPPER(t.status) IN ('CREATED','PENDING')
    AND UPPER(COALESCE(t.action, '')) NOT IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'DSC_SIGN_IC', 'CANCEL', 'WITHDRAW', 'REJECT')
    AND UPPER(COALESCE(t.jobStatus, '')) NOT IN ('COMPLETED', 'FINISH', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'GENERATED', 'DSC_SIGN_IC', 'IC_SIGNED', 'CANCEL', 'CANCELLED', 'WITHDRAW', 'WITHDRAWN', 'REJECT', 'REJECTED')
    AND t.nextRole = :roleName
""" )
   List<SleeperWorkflowTransaction> findLastPendingRequestsByRole(String roleName);


    boolean existsByModuleIdAndRequestId(Long moduleId, String requestId);

    List<SleeperWorkflowTransaction> findByModuleId(Long moduleId);

    @Query("SELECT DISTINCT t.requestId FROM SleeperWorkflowTransaction t WHERE t.moduleId = :moduleId")
    List<String> findRequestIdsByModuleId(@Param("moduleId") Long moduleId);

    @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId IN (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        WHERE t2.moduleId = :moduleId
        GROUP BY t2.requestId
    )
      AND t.moduleId = :moduleId
      AND UPPER(t.status) IN ('CREATED', 'PENDING', 'IN-PROGRESS', 'RESUBMITTED')
      AND (t.nextRole IS NULL OR UPPER(t.nextRole) = UPPER(:roleName))
      AND (
          :plantId IS NULL OR :plantId = '' 
          OR t.plantId IS NULL OR t.plantId = '' 
          OR LOWER(t.plantId) LIKE LOWER(CONCAT('%', :plantId, '%')) 
          OR LOWER(:plantId) LIKE LOWER(CONCAT('%', t.plantId, '%'))
          OR REPLACE(LOWER(t.plantId), ':', '') = REPLACE(LOWER(:plantId), ':', '')
      )
    ORDER BY t.workflowTransitionId DESC
""")
    Page<SleeperWorkflowTransaction> findLastPendingRequestsByRole(
            @Param("roleName") String roleName,
            @Param("moduleId") Integer moduleId,
            @Param("plantId") String plantId,
            Pageable pageable);


    @Query("""
SELECT t FROM SleeperWorkflowTransaction t
WHERE t.workflowTransitionId IN (
    SELECT MAX(t2.workflowTransitionId)
    FROM SleeperWorkflowTransaction t2
    GROUP BY t2.requestId
)
AND UPPER(t.status) IN ('CREATED','PENDING')
AND UPPER(COALESCE(t.action, '')) NOT IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'DSC_SIGN_IC', 'CANCEL', 'WITHDRAW', 'REJECT')
AND UPPER(COALESCE(t.jobStatus, '')) NOT IN ('COMPLETED', 'FINISH', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'GENERATED', 'DSC_SIGN_IC', 'IC_SIGNED', 'CANCEL', 'CANCELLED', 'WITHDRAW', 'WITHDRAWN', 'REJECT', 'REJECTED')
AND t.nextRole = :roleName
ORDER BY t.workflowTransitionId DESC
""")
    List<SleeperWorkflowTransaction> findLatestByRole(String roleName);

    @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId IN (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        GROUP BY t2.requestId
    )
    AND UPPER(t.status) IN ('CREATED','PENDING')
    AND UPPER(COALESCE(t.action, '')) NOT IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'DSC_SIGN_IC', 'CANCEL', 'WITHDRAW', 'REJECT')
    AND UPPER(COALESCE(t.jobStatus, '')) NOT IN ('COMPLETED', 'FINISH', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'GENERATED', 'DSC_SIGN_IC', 'IC_SIGNED', 'CANCEL', 'CANCELLED', 'WITHDRAW', 'WITHDRAWN', 'REJECT', 'REJECTED')
    AND t.nextRole = :roleName
    AND (:assignedTo IS NULL OR t.assignedToUser = :assignedTo)
    AND (
        :plantId IS NULL OR :plantId = '' 
        OR t.plantId IS NULL OR t.plantId = '' 
        OR LOWER(t.plantId) LIKE LOWER(CONCAT('%', :plantId, '%')) 
        OR LOWER(:plantId) LIKE LOWER(CONCAT('%', t.plantId, '%'))
        OR REPLACE(LOWER(COALESCE(t.plantId, '')), ':', '') = REPLACE(LOWER(:plantId), ':', '')
    )
    ORDER BY t.workflowTransitionId DESC
    """)
    List<SleeperWorkflowTransaction> findLatestByRoleAndAssignedTo(
            @Param("roleName") String roleName,
            @Param("assignedTo") Long assignedTo,
            @Param("plantId") String plantId);

    @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId IN (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        GROUP BY t2.requestId
    )
    AND UPPER(t.status) IN ('CREATED','PENDING')
    AND UPPER(COALESCE(t.action, '')) NOT IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'DSC_SIGN_IC', 'CANCEL', 'WITHDRAW', 'REJECT')
    AND UPPER(COALESCE(t.jobStatus, '')) NOT IN ('COMPLETED', 'FINISH', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'GENERATED', 'DSC_SIGN_IC', 'IC_SIGNED', 'CANCEL', 'CANCELLED', 'WITHDRAW', 'WITHDRAWN', 'REJECT', 'REJECTED')
    AND t.nextRole = :roleName
    AND (
        :rio IS NULL OR :rio = '' 
        OR t.rio = :rio 
        OR LOWER(t.rio) = LOWER(:rio)
    )
    AND (
        :plantId IS NULL OR :plantId = '' 
        OR t.plantId IS NULL OR t.plantId = '' 
        OR LOWER(t.plantId) LIKE LOWER(CONCAT('%', :plantId, '%')) 
        OR LOWER(:plantId) LIKE LOWER(CONCAT('%', t.plantId, '%'))
        OR REPLACE(LOWER(COALESCE(t.plantId, '')), ':', '') = REPLACE(LOWER(:plantId), ':', '')
    )
    ORDER BY t.workflowTransitionId DESC
    """)
    List<SleeperWorkflowTransaction> findLatestByRoleAndRio(
            @Param("roleName") String roleName,
            @Param("rio") String rio,
            @Param("plantId") String plantId);

    @Query("""
                SELECT t FROM SleeperWorkflowTransaction t
                WHERE t.workflowTransitionId IN (
                    SELECT MAX(t2.workflowTransitionId)
                    FROM SleeperWorkflowTransaction t2
                    GROUP BY t2.requestId
                )
                AND t.status = 'COMPLETED'
                ORDER BY t.workflowTransitionId DESC
            """)
    List<SleeperWorkflowTransaction> findLastCompletedRequests();

    @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId IN (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        GROUP BY t2.requestId, COALESCE(t2.moduleId, 0)
    )
    AND t.status = 'Completed'
    ORDER BY t.workflowTransitionId DESC
""")
   List<SleeperWorkflowTransaction> findCompletedRequests();

    @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId IN (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        WHERE t2.moduleId = :moduleId
        GROUP BY t2.requestId
    )
      AND t.moduleId = :moduleId
      AND t.status = 'Completed'
      AND (:plantId IS NULL OR :plantId = '' OR t.plantId = :plantId OR REPLACE(COALESCE(t.plantId, ''), ':', '') = REPLACE(:plantId, ':', ''))
    ORDER BY t.workflowTransitionId DESC
""")
    Page<SleeperWorkflowTransaction> findCompletedRequests(
            @Param("moduleId") Integer moduleId,
            @Param("plantId") String plantId,
            Pageable pageable);

    @Query("""
    SELECT t FROM SleeperWorkflowTransaction t
    WHERE t.workflowTransitionId IN (
        SELECT MAX(t2.workflowTransitionId)
        FROM SleeperWorkflowTransaction t2
        WHERE t2.workflowId = 2
        GROUP BY t2.requestId
    )
    AND (
        UPPER(COALESCE(t.status, '')) IN ('COMPLETED', 'IC_ISSUE', 'IC_GENERATION', 'GENERATED', 'DSC_SIGN_IC', 'IC_SIGNED', 'CANCEL', 'CANCELLED', 'WITHDRAW', 'WITHDRAWN', 'REJECT', 'REJECTED')
        OR UPPER(COALESCE(t.action, '')) IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'DSC_SIGN_IC', 'CANCEL', 'WITHDRAW', 'REJECT')
        OR UPPER(COALESCE(t.jobStatus, '')) IN ('COMPLETED', 'FINISH', 'IC_ISSUE', 'ISSUE IC', 'GENERATE_IC', 'IC_GENERATION', 'GENERATED', 'DSC_SIGN_IC', 'IC_SIGNED', 'CANCEL', 'CANCELLED', 'WITHDRAW', 'WITHDRAWN', 'REJECT', 'REJECTED')
    )
    AND t.workflowId = 2
    ORDER BY t.workflowTransitionId DESC
""")
    List<SleeperWorkflowTransaction> findFinalCompletedRequests();

    @Query("""
SELECT t.requestId FROM SleeperWorkflowTransaction t
WHERE t.workflowTransitionId IN (
    SELECT MAX(t2.workflowTransitionId)
    FROM SleeperWorkflowTransaction t2
    GROUP BY t2.requestId
)
AND t.moduleId = :moduleId
AND t.status = 'Completed'
""")
    List<String> findCompletedRequestIdsByModuleId(@Param("moduleId") Long moduleId);

    @Query("""
        SELECT t FROM SleeperWorkflowTransaction t
        WHERE t.workflowTransitionId = (
            SELECT MAX(t2.workflowTransitionId)
            FROM SleeperWorkflowTransaction t2
            WHERE t2.requestId = t.requestId
              AND t2.workflowId = 2
        )
        AND t.workflowId = 2
    """)
    List<SleeperWorkflowTransaction> findLatestTransactionsForWorkflow2();

    @Query("""
        SELECT t FROM SleeperWorkflowTransaction t
        WHERE t.workflowTransitionId = (
            SELECT MAX(t2.workflowTransitionId)
            FROM SleeperWorkflowTransaction t2
            WHERE t2.requestId = t.requestId
              AND t2.workflowId = 2
        )
        AND t.workflowId = 2
        AND t.plantId = :plantId
    """)
    List<SleeperWorkflowTransaction> findLatestTransactionsForWorkflow2ByPlantId(@Param("plantId") String plantId);

    @Query("""
        SELECT t FROM SleeperWorkflowTransaction t
        WHERE t.workflowTransitionId = (
            SELECT MAX(t2.workflowTransitionId)
            FROM SleeperWorkflowTransaction t2
            WHERE t2.requestId = t.requestId
              AND t2.workflowId = 2
        )
        AND t.workflowId = 2
        AND t.plantId IN :plantIds
    """)
    List<SleeperWorkflowTransaction> findLatestTransactionsForWorkflow2ByPlantIds(@Param("plantIds") java.util.Collection<String> plantIds);

    @Query("""
        SELECT t FROM SleeperWorkflowTransaction t
        WHERE t.workflowTransitionId IN (
            SELECT MAX(t2.workflowTransitionId)
            FROM SleeperWorkflowTransaction t2
            WHERE t2.workflowId = 2
            GROUP BY t2.requestId
        )
        AND t.workflowId = 2
        AND t.action IN :actions
    """)
    List<SleeperWorkflowTransaction> findPendingVerifiedCalls(@Param("actions") java.util.List<String> actions);

    @Query(value = """
                SELECT status 
                FROM sleeper_workflow_transaction 
                WHERE workflow_transition_id = (
                    SELECT MAX(workflow_transition_id)
                    FROM sleeper_workflow_transaction
                    WHERE request_id = :requestId 
                      AND module_id = :moduleId
                )
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

  /*  @Query("SELECT s.requestId FROM SleeperWorkflowTransaction s WHERE s.moduleId = 11 AND s.status = 'COMPLETED' AND s.requestId IN :requestIds")
    List<String> findCompletedWorkflowsByRequestIds(@Param("requestIds") List<String> requestIds);
*/
  @Query("""
SELECT s.requestId
FROM SleeperWorkflowTransaction s
WHERE s.moduleId = 11
AND s.requestId IN :requestIds
AND s.workflowTransitionId = (
    SELECT MAX(sw.workflowTransitionId)
    FROM SleeperWorkflowTransaction sw
    WHERE sw.requestId = s.requestId
    AND sw.moduleId = s.moduleId
)
AND s.status = 'COMPLETED'
""")
  List<String> findCompletedWorkflowsByRequestIds(@Param("requestIds") List<String> requestIds);
   
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
AND s.workflowTransitionId IN (
    SELECT MAX(s2.workflowTransitionId)
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
AND s.workflowTransitionId IN (
    SELECT MAX(s2.workflowTransitionId)
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

    @Query(value = """
        SELECT
            sic.call_no AS inspectionCallNumber,
            COALESCE(vp.company_name, vp.plant_name, ph.vendor_details, ph.firm_details, 'N/A') AS vendor,
            DATE_FORMAT(sic.created_at, '%d/%m/%Y %H:%i:%s') AS callSubmissionDateTime,
            'Final Stage' AS stageOfInspection,
            CONCAT(COALESCE(ph.rly_short_name, ph.rly_cd, 'N/A'), '/', sic.po_no, '/', COALESCE(sic.sr_no, 'N/A')) AS poSrNo,
            DATE_FORMAT(pi.delivery_date, '%d/%m/%Y') AS dpDate,
            CASE
                WHEN t.action IN ('INITIATE_CALL', 'PO_VERIFICATION', 'PAUSE', 'WITHHELD') OR UPPER(t.job_status) IN ('INITIATED', 'PO_VERIFICATION', 'PAUSED', 'WITHHELD') THEN 'Under Inspection'
                WHEN t.action IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'IC_GENERATION') OR UPPER(t.job_status) IN ('COMPLETED', 'IC_ISSUE', 'GENERATED') THEN 'Completed'
                ELSE 'Pending'
            END AS status,
            CASE
                WHEN t.action IN ('INITIATE_CALL', 'PO_VERIFICATION', 'PAUSE', 'WITHHELD') OR UPPER(t.job_status) IN ('INITIATED', 'PO_VERIFICATION', 'PAUSED', 'WITHHELD') THEN 'Under Inspection'
                WHEN t.action IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'IC_GENERATION') OR UPPER(t.job_status) IN ('COMPLETED', 'IC_ISSUE', 'GENERATED') THEN 'Completed'
                ELSE 'Pending'
            END AS mainStatus,
            CASE
                WHEN t.action = 'PO_VERIFICATION' OR UPPER(t.job_status) = 'PO_VERIFICATION' THEN 'PO Verification'
                WHEN t.action = 'PAUSE' OR UPPER(t.job_status) = 'PAUSED' THEN 'Paused'
                WHEN t.action = 'INITIATE_CALL' OR UPPER(t.job_status) = 'INITIATED' THEN 'Initiated'
                WHEN t.action = 'WITHHELD' OR UPPER(t.job_status) = 'WITHHELD' THEN 'Withheld'
                WHEN t.action = 'MAIN_IE_SCHEDULE_CALL' OR UPPER(t.job_status) = 'SCHEDULED' THEN 'Scheduled'
                WHEN t.action = 'VERIFY' OR UPPER(t.job_status) = 'RIO_VERIFIED' THEN 'Assigned to IE'
                WHEN t.action IN ('CREATE', 'CREATED', 'CALL_CREATED') OR UPPER(t.job_status) = 'CREATED' THEN 'Call Created'
                ELSE COALESCE(t.action, t.job_status, 'Under Inspection')
            END AS subStatus
        FROM sleeper_inspection_call sic
        LEFT JOIN (
            SELECT
                swt1.request_id,
                swt1.action,
                swt1.job_status,
                swt1.plant_id,
                swt1.vendor_code,
                swt1.poi_code
            FROM sleeper_workflow_transaction swt1
            INNER JOIN (
                SELECT request_id, MAX(workflow_transition_id) AS max_id
                FROM sleeper_workflow_transaction
                WHERE workflow_id = 2
                GROUP BY request_id
            ) latest ON swt1.request_id = latest.request_id AND swt1.workflow_transition_id = latest.max_id
        ) t ON t.request_id COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci
        LEFT JOIN vendor_plant vp ON (vp.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci 
            OR vp.plant_id COLLATE utf8mb4_unicode_ci = REPLACE(sic.plant_id, ':', '') COLLATE utf8mb4_unicode_ci)
        LEFT JOIN po_header ph ON (ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci 
            OR ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN po_item pi ON (pi.po_header_id = ph.id 
            AND (pi.item_sr_no COLLATE utf8mb4_unicode_ci = sic.sr_no COLLATE utf8mb4_unicode_ci 
                 OR pi.item_sr_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.sr_no, '/', -1) COLLATE utf8mb4_unicode_ci))
        WHERE (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR
               sic.plant_id = :vendorPlantCode OR
               vp.company_name = :vendorPlantCode OR
               vp.vendor_code = :vendorPlantCode)
          AND (:zonalRailway IS NULL OR :zonalRailway = '' OR vp.zonal_railway = :zonalRailway OR ph.rly_short_name = :zonalRailway)
          AND (:startDate IS NULL OR :endDate IS NULL OR sic.created_at BETWEEN :startDate AND :endDate)
          AND (
               :stage = 'ALL' OR :stage IS NULL OR :stage = '' OR :stage = 'Final' OR :stage = 'Final Stage'
          )
          AND (
               :status = 'ALL' OR
               (:status = 'Open' AND (
                   t.action IS NULL OR
                   t.action IN ('CREATE', 'CREATED', 'CALL_CREATED', 'VERIFY', 'MAIN_IE_SCHEDULE_CALL', 'INITIATE_CALL', 'PO_VERIFICATION', 'PAUSE', 'WITHHELD', 'RESCHEDULE_CALL')
                   OR UPPER(COALESCE(t.job_status, '')) IN ('CREATED', 'RIO_VERIFIED', 'SCHEDULED', 'INITIATED', 'PO_VERIFICATION', 'PAUSED', 'WITHHELD', 'RESCHEDULE')
                   OR (t.action NOT IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'IC_GENERATION') AND UPPER(COALESCE(t.job_status, '')) NOT IN ('COMPLETED', 'IC_ISSUE', 'GENERATED'))
               )) OR
               (:status = 'Under Inspection' AND (
                   t.action IN ('INITIATE_CALL', 'PO_VERIFICATION', 'PAUSE', 'WITHHELD')
                   OR UPPER(t.job_status) IN ('INITIATED', 'PO_VERIFICATION', 'PAUSED', 'WITHHELD')
               )) OR
               (:status = 'Pending' AND (
                   t.action IS NULL
                   OR t.action IN ('CREATE', 'CREATED', 'CALL_CREATED', 'VERIFY', 'MAIN_IE_SCHEDULE_CALL', 'RESCHEDULE_CALL')
                   OR UPPER(t.job_status) IN ('CREATED', 'RIO_VERIFIED', 'SCHEDULED', 'RESCHEDULE')
               )) OR
                ((:status = 'Completed' OR :status = 'IC Issued') AND (
                    UPPER(COALESCE(t.job_status, '')) = 'IC_GENERATION'
                ))
           )
         ORDER BY sic.created_at DESC
     """, nativeQuery = true)
    List<Object[]> getSleeperInspectionCallStatusDetailsFiltered(
            @Param("stage") String stage,
            @Param("status") String status,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
        SELECT
            COALESCE(vp.company_name, vp.plant_name, ph.vendor_details, ph.firm_details, 'N/A') AS vendorName,
            COALESCE(ph.rly_short_name, ph.rly_cd, 'N/A') AS railwayShortName,
            SUBSTRING_INDEX(sic.po_no, '/', 1) AS poNumberOnly,
            COALESCE(sic.sr_no, 'N/A') AS poSerialNumber,
            sic.call_no AS callNumber,
            COALESCE(sicd.certificate_no, CONCAT('C/', sic.call_no, '/NV')) AS icNumber,
            'Final' AS stage,
            DATE_FORMAT(COALESCE(sicd.created_on, swt.created_date), '%Y-%m-%d') AS icIssuedDate,
            COALESCE(ph.item_cat_descr, 'PSC Mainline Sleeper') AS itemCatDescr,
            COALESCE(sicd.created_on, swt.created_date) AS rawCreatedDate
        FROM sleeper_workflow_transaction swt
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON swt.request_id = latest.request_id AND swt.workflow_transition_id = latest.max_id
        INNER JOIN sleeper_inspection_call sic ON swt.request_id COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci
        LEFT JOIN vendor_plant vp ON (vp.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
            OR vp.plant_id COLLATE utf8mb4_unicode_ci = REPLACE(sic.plant_id, ':', '') COLLATE utf8mb4_unicode_ci)
        LEFT JOIN po_header ph ON (ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
            OR ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN sleeper_inspection_complete_details sicd ON sic.call_no COLLATE utf8mb4_unicode_ci = sicd.call_no COLLATE utf8mb4_unicode_ci
        WHERE UPPER(COALESCE(swt.job_status, '')) = 'IC_GENERATION'
          AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR
               sic.plant_id = :vendorPlantCode OR
               vp.company_name = :vendorPlantCode OR
               vp.vendor_code = :vendorPlantCode)
          AND (:zonalRailway IS NULL OR :zonalRailway = '' OR vp.zonal_railway = :zonalRailway OR ph.rly_short_name = :zonalRailway)
          AND (:startDate IS NULL OR DATE(COALESCE(sicd.created_on, swt.created_date)) >= :startDate)
          AND (:endDate IS NULL OR DATE(COALESCE(sicd.created_on, swt.created_date)) <= :endDate)
        ORDER BY rawCreatedDate DESC
    """, nativeQuery = true)
    List<Object[]> findSleeperDownloadIcAnnexuresReportRaw(
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    @Query(value = """
        SELECT COUNT(DISTINCT swt.request_id)
        FROM sleeper_workflow_transaction swt
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON swt.request_id = latest.request_id AND swt.workflow_transition_id = latest.max_id
        INNER JOIN sleeper_inspection_call sic ON swt.request_id COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci
        LEFT JOIN vendor_plant vp ON (vp.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
            OR vp.plant_id COLLATE utf8mb4_unicode_ci = REPLACE(sic.plant_id, ':', '') COLLATE utf8mb4_unicode_ci)
        LEFT JOIN po_header ph ON (ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
            OR ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN sleeper_inspection_complete_details sicd ON sic.call_no COLLATE utf8mb4_unicode_ci = sicd.call_no COLLATE utf8mb4_unicode_ci
        WHERE UPPER(COALESCE(swt.job_status, '')) = 'IC_GENERATION'
          AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR
               sic.plant_id = :vendorPlantCode OR
               vp.company_name = :vendorPlantCode OR
               vp.vendor_code = :vendorPlantCode)
          AND (:zonalRailway IS NULL OR :zonalRailway = '' OR vp.zonal_railway = :zonalRailway OR ph.rly_short_name = :zonalRailway)
          AND (:startDate IS NULL OR :endDate IS NULL OR sic.created_at BETWEEN :startDate AND :endDate)
    """, nativeQuery = true)
    Long countSleeperIcIssuedFiltered(
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
        SELECT COUNT(DISTINCT swt.request_id)
        FROM sleeper_workflow_transaction swt
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON swt.request_id = latest.request_id AND swt.workflow_transition_id = latest.max_id
        INNER JOIN sleeper_inspection_call sic ON swt.request_id COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci
        LEFT JOIN sleeper_inspection_complete_details sicd ON sic.call_no COLLATE utf8mb4_unicode_ci = sicd.call_no COLLATE utf8mb4_unicode_ci
        WHERE UPPER(COALESCE(swt.job_status, '')) = 'IC_GENERATION'
          AND (:plantIds IS NULL OR sic.plant_id IN (:plantIds) OR REPLACE(sic.plant_id, ':', '') IN (:plantIds))
    """, nativeQuery = true)
    Long countSleeperIcIssuedByPlantIds(@Param("plantIds") List<String> plantIds);
}