package com.sarthi.repository;

import com.sarthi.entity.WorkflowTransition;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, Integer> {
    WorkflowTransition findByWorkflowIdAndRequestId(Integer workflowId, String requestId);
    List<WorkflowTransition> findByRequestIdOrderByWorkflowTransitionIdAsc(String requestId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE WorkflowTransition wt SET wt.assignedToUser = :newUser WHERE wt.workflowTransitionId = (SELECT w2.workflowTransitionId FROM (SELECT MAX(workflowTransitionId) AS workflowTransitionId FROM WorkflowTransition WHERE requestId = :callNo) w2)")
    int updateAssignedToUserForLatestTransaction(@Param("callNo") String callNo, @Param("newUser") Integer newUser);

    @Query(value = """
            SELECT
                SUM(CASE WHEN wt.REQUESTID LIKE 'ER-%' THEN 1 ELSE 0 END) AS rmCount,
                SUM(CASE WHEN wt.REQUESTID LIKE 'EP-%' THEN 1 ELSE 0 END) AS processCount,
                SUM(CASE WHEN wt.REQUESTID LIKE 'EF-%' THEN 1 ELSE 0 END) AS finalCount
            FROM workflow_transition wt
            INNER JOIN (
                SELECT requestid, MAX(workflowtransitionid) latest_id
                FROM workflow_transition
                GROUP BY requestid
            ) x ON wt.workflowtransitionid = x.latest_id
            JOIN inspection_calls ic ON wt.REQUESTID = ic.ic_number
            JOIN po_header ph ON ic.po_no = ph.po_no
            WHERE wt.STATUS = 'DSC_SIGN_IC'
            AND (:poiCode IS NULL OR :poiCode = '' OR ic.place_of_inspection = :poiCode)
            AND (:rlyShortName IS NULL OR :rlyShortName = '' OR ph.rly_short_name = :rlyShortName)
            AND (:startDate IS NULL OR :startDate = '' OR wt.CREATEDDATE >= :startDate)
            AND (:endDate IS NULL OR :endDate = '' OR wt.CREATEDDATE <= :endDate)
            """, nativeQuery = true)
    Map<String, Object> getIcIssuedCounts(
            @Param("poiCode") String poiCode,
            @Param("rlyShortName") String rlyShortName,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Query(value = "SELECT * FROM WORKFLOW_TRANSITION WHERE WORKFLOWTRANSITIONID IN (" +
            "  SELECT MAX(WORKFLOWTRANSITIONID) " +
            "  FROM WORKFLOW_TRANSITION " +
            "  WHERE (:rio IS NULL OR RIO = :rio) " +
            "  GROUP BY REQUESTID" +
            ")", nativeQuery = true)
    List<WorkflowTransition> findLatestByRio(@Param("rio") String rio);

    // @Query("SELECT COUNT(w) FROM WorkflowTransition w " +
    // "WHERE w.assignedToUser = :ieUserId AND w.status IN
    // ('VERIFIED','ASSIGNED','INITIATED')")
    // int countActiveCallsForIE(@Param("ieUserId") Integer ieUserId);
    @Query("SELECT COUNT(w) FROM WorkflowTransition w " +
            "WHERE w.workflowTransitionId IN (" +
            "   SELECT MAX(w2.workflowTransitionId) FROM WorkflowTransition w2 " +
            "   WHERE w2.assignedToUser = :ieUserId GROUP BY w2.requestId" +
            ") AND w.jobStatus IN ('ASSIGNED','IN_PROGRESS','VERIFIED','INITIATED')")
    int countActiveCallsForIE(@Param("ieUserId") Integer ieUserId);

    @Query(value = "SELECT rio FROM WORKFLOW_TRANSITION WHERE requestid = :callNo AND job_status = 'Created' ORDER BY WORKFLOWTRANSITIONID ASC LIMIT 1", nativeQuery = true)
    String findRioByCallNoAndStatusCreated(@Param("callNo") String callNo);

    WorkflowTransition findTopByRequestIdOrderByWorkflowTransitionIdDesc(String requestId);

    WorkflowTransition findFirstByRequestIdAndRioIsNotNullOrderByWorkflowTransitionIdDesc(String requestId);

    List<WorkflowTransition> findByRequestIdOrderByWorkflowTransitionIdDesc(String requestId);

    @Query(value = "SELECT * FROM WORKFLOW_TRANSITION WHERE WORKFLOWTRANSITIONID IN (" +
            "  SELECT MAX(WORKFLOWTRANSITIONID) " +
            "  FROM WORKFLOW_TRANSITION " +
            "  WHERE REQUESTID IN (:requestIds) " +
            "  GROUP BY REQUESTID" +
            ")", nativeQuery = true)
    List<WorkflowTransition> findLatestByRequestIds(@Param("requestIds") List<String> requestIds);

    /*
     * @Query("SELECT wt FROM WorkflowTransition wt " +
     * "WHERE wt.workflowTransitionId IN (" +
     * "    SELECT MAX(wt2.workflowTransitionId) " +
     * "    FROM WorkflowTransition wt2 " +
     * "    GROUP BY wt2.requestId" +
     * ") " +
     * "AND wt.nextRoleName = :roleName " +
     * "AND wt.jobStatus IN ('IN_PROGRESS','VERIFIED','APPROVED','REGISTERED','Created','ASSIGNED','REJECTED','PAUSED')"
     * )
     * List<WorkflowTransition> findPendingByRole(@Param("roleName") String
     * roleName);
     * 
     */
    @Query("SELECT wt FROM WorkflowTransition wt " +
            "WHERE wt.workflowTransitionId IN (" +
            "   SELECT MAX(wt2.workflowTransitionId) " +
            "   FROM WorkflowTransition wt2 " +
            "   WHERE wt2.transitionId NOT IN (42,44,45) " + // Exclude qty edit transitions
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
            @Param("roleNames") List<String> roleNames);

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
            @Param("roleName") String roleName);

    WorkflowTransition findTopByRequestIdAndStatus(String requestId, String initiateInspection);

    WorkflowTransition findTopByRequestIdAndStatusOrderByWorkflowTransitionIdDesc(String requestId,
            String callRegistered);

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
                        OR wt.modifiedBy = :userId)
                   )
                   OR
                   (wt.requestId NOT LIKE 'EP%'
                        AND wt.modifiedBy = :userId
                   )
              )
            """)
    List<WorkflowTransition> findCompletedByUserRule(
            @Param("userId") Long userId);

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
                        OR wt.modifiedBy = :userId)
                   )
                   OR
                   (wt.requestId NOT LIKE 'EP%'
                        AND wt.modifiedBy = :userId
                   )
              )
            """)
    List<WorkflowTransition> findSignedByUserRule(
            @Param("userId") Long userId);

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
            @Param("requestIds") List<String> requestIds);

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

    /*
     * @Query(value = """
     * SELECT 'RM' as category,
     * COUNT(DISTINCT CASE WHEN has_initiate = 1 AND is_complete = 0 THEN requestId
     * END) as under,
     * COUNT(DISTINCT CASE WHEN has_created = 1 AND has_initiate = 0 AND is_complete
     * = 0 THEN requestId END) as pending
     * FROM (
     * SELECT requestId,
     * MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END)
     * as has_initiate,
     * MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) as has_created,
     * MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) as
     * is_complete
     * FROM workflow_transition
     * WHERE requestId LIKE '%ER%'
     * AND requestId NOT IN (SELECT ic_number FROM inspection_calls WHERE po_no =
     * :excludePo)
     * GROUP BY requestId
     * ) t1
     * UNION ALL
     * SELECT 'Process' as category,
     * COUNT(DISTINCT CASE WHEN has_initiate = 1 AND is_complete = 0 THEN requestId
     * END) as under,
     * COUNT(DISTINCT CASE WHEN has_created = 1 AND has_initiate = 0 AND is_complete
     * = 0 THEN requestId END) as pending
     * FROM (
     * SELECT requestId,
     * MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END)
     * as has_initiate,
     * MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) as has_created,
     * MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) as
     * is_complete
     * FROM workflow_transition
     * WHERE requestId LIKE '%EP%'
     * AND requestId NOT IN (SELECT ic_number FROM inspection_calls WHERE po_no =
     * :excludePo)
     * GROUP BY requestId
     * ) t2
     * UNION ALL
     * SELECT 'Final' as category,
     * COUNT(DISTINCT CASE WHEN has_initiate = 1 AND is_complete = 0 THEN requestId
     * END) as under,
     * COUNT(DISTINCT CASE WHEN has_created = 1 AND has_initiate = 0 AND is_complete
     * = 0 THEN requestId END) as pending
     * FROM (
     * SELECT requestId,
     * MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END)
     * as has_initiate,
     * MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) as has_created,
     * MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) as
     * is_complete
     * FROM workflow_transition
     * WHERE requestId LIKE '%EF%'
     * AND requestId NOT IN (SELECT ic_number FROM inspection_calls WHERE po_no =
     * :excludePo)
     * GROUP BY requestId
     * ) t3
     * """, nativeQuery = true)
     * List<Object[]> getInspectionCallStatusBreakdownExcludingDummyPo(
     * 
     * @Param("excludePo") String excludePo,
     * 
     * @Param("poiCode") String poiCode,
     * 
     * @Param("rlyShortName") String rlyShortName,
     * 
     * @Param("startDate") String startDate,
     * 
     * @Param("endDate") String endDate);
     */
    @Query(value = """

            SELECT
                stage.category,

                COUNT(
                                     CASE
                                         WHEN (
                                             stage.category = 'Process'
                                             AND stage.latest_status IN (
                                                 'PAUSED',
                                                 'PAUSE_INSPECTION_RESUME_NEXT_DAY',
                                                 'ENTER_SHIFT_DETAILS_AND_START_INSPECTION'
                                             )
                                         )
                                         OR (
                                             stage.category <> 'Process'
                                             AND stage.latest_status IN (
                                                 'VERIFY_PO_DETAILS',
                                                 'PAUSED',
                                                 'PAUSE_INSPECTION_RESUME_NEXT_DAY',
                                                 'ENTER_SHIFT_DETAILS_AND_START_INSPECTION'
                                             )
                                         )
                                         THEN 1
                                     END
                                 ) AS under_count,

                COUNT(
                    CASE
                        WHEN stage.latest_status IN (
                            'CREATED',
                            'Created',
                            'VERIFIED',
                            'RETURNED',
                            'CALL_REGISTERED',
                            'IE_SCHEDULED',
                            'INITIATE_INSPECTION'
                        )
                        THEN 1
                    END
                ) AS pending_count

            FROM (

                SELECT
                    ic.ic_number,

                    CASE
                        WHEN ic.ic_number LIKE '%ER%' THEN 'RM'
                        WHEN ic.ic_number LIKE '%EP%' THEN 'Process'
                        WHEN ic.ic_number LIKE '%EF%' THEN 'Final'
                    END AS category,

                    UPPER(wt.STATUS) AS latest_status

                FROM inspection_calls ic

                INNER JOIN (

                    SELECT
                        REQUESTID,
                        MAX(WORKFLOWTRANSITIONID) AS latest_transition_id

                    FROM workflow_transition

                    GROUP BY REQUESTID

                ) latest
                    ON latest.REQUESTID = ic.ic_number
                INNER JOIN workflow_transition wt
                    ON wt.WORKFLOWTRANSITIONID = latest.latest_transition_id
                LEFT JOIN po_header ph ON ic.po_no = ph.po_no
                WHERE ic.po_no <> :excludePo
                  AND (:poiCode IS NULL OR :poiCode = '' OR ic.place_of_inspection = :poiCode)
                  AND (:rlyShortName IS NULL OR :rlyShortName = '' OR ph.rly_short_name = :rlyShortName)
                  AND (:startDate IS NULL OR :startDate = '' OR wt.CREATEDDATE >= :startDate)
                  AND (:endDate IS NULL OR :endDate = '' OR wt.CREATEDDATE <= :endDate)
                  AND UPPER(wt.STATUS) NOT IN (
                        'INSPECTION_COMPLETE_CONFIRM',
                        'WITHDRAW',
                        'CANCELLED'
                  )
                  AND (
                        ic.ic_number LIKE '%ER%'
                     OR ic.ic_number LIKE '%EP%'
                     OR ic.ic_number LIKE '%EF%'
                  )

            ) stage

            GROUP BY stage.category

            ORDER BY
                CASE stage.category
                    WHEN 'RM' THEN 1
                    WHEN 'Process' THEN 2
                    WHEN 'Final' THEN 3
                END

            """, nativeQuery = true)
    List<Object[]> getInspectionCallStatusBreakdownExcludingDummyPo(
            @Param("excludePo") String excludePo,
            @Param("poiCode") String poiCode,
            @Param("rlyShortName") String rlyShortName,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    /*
     * @Query(value = """
     * 
     * SELECT
     * ic.ic_number AS inspectionCallNumber,
     * 
     * ic.company_name AS vendor,
     * 
     * DATE_FORMAT(
     * ic.created_at,
     * '%d/%m/%Y %H:%i:%s'
     * ) AS callSubmissionDateTime,
     * 
     * CASE
     * WHEN ic.ic_number LIKE '%ER%' THEN 'RM Stage'
     * WHEN ic.ic_number LIKE '%EP%' THEN 'Process Stage'
     * WHEN ic.ic_number LIKE '%EF%' THEN 'Final Stage'
     * ELSE 'Other'
     * END AS stageOfInspection,
     * 
     * CONCAT(
     * COALESCE(ph.rly_short_name, ph.rly_cd, 'N/A'),
     * '/',
     * ic.po_no,
     * '/',
     * COALESCE(ic.po_serial_no, 'N/A')
     * ) AS poSrNo,
     * 
     * DATE_FORMAT(
     * pi.delivery_date,
     * '%d/%m/%Y'
     * ) AS dpDate,
     * 
     * CASE
     * WHEN UPPER(wt.STATUS) IN (
     * 'ENTER_SHIFT_DETAILS_AND_START_INSPECTION',
     * 'PAUSED'
     * )
     * THEN 'Under Inspection'
     * 
     * WHEN UPPER(wt.STATUS) IN (
     * 'CREATED',
     * 'VERIFIED',
     * 'CALL_REGISTERED',
     * 'IE_SCHEDULED',
     * 'INITIATE_INSPECTION',
     * 'VERIFY_PO_DETAILS',
     * 'WITHHELD'
     * )
     * THEN 'Pending'
     * 
     * ELSE 'Pending'
     * END AS status
     * 
     * FROM inspection_calls ic
     * 
     * INNER JOIN (
     * SELECT
     * REQUESTID,
     * MAX(WORKFLOWTRANSITIONID) AS latest_transition_id
     * FROM workflow_transition
     * GROUP BY REQUESTID
     * ) latest
     * ON latest.REQUESTID = ic.ic_number
     * 
     * INNER JOIN workflow_transition wt
     * ON wt.WORKFLOWTRANSITIONID =
     * latest.latest_transition_id
     * 
     * LEFT JOIN po_header ph
     * ON ph.po_no = ic.po_no
     * 
     * LEFT JOIN po_item pi
     * ON pi.po_header_id = ph.id
     * AND pi.item_sr_no = ic.po_serial_no
     * 
     * WHERE
     * 
     * ic.po_no <> 'DummyPo_001'
     * 
     * AND UPPER(wt.STATUS) NOT IN (
     * 'INSPECTION_COMPLETE_CONFIRM',
     * 'WITHDRAW',
     * 'CANCELLED'
     * )
     * 
     * AND (
     * (:stage = 'RM'
     * AND ic.ic_number LIKE '%ER%')
     * OR
     * (:stage = 'Process'
     * AND ic.ic_number LIKE '%EP%')
     * OR
     * (:stage = 'Final'
     * AND ic.ic_number LIKE '%EF%')
     * )
     * 
     * AND (
     * :status = 'ALL'
     * 
     * OR
     * 
     * (
     * :status = 'Under Inspection'
     * AND UPPER(wt.STATUS) IN (
     * 'ENTER_SHIFT_DETAILS_AND_START_INSPECTION',
     * 'PAUSED'
     * )
     * )
     * 
     * OR
     * 
     * (
     * :status = 'Pending'
     * AND UPPER(wt.STATUS) IN (
     * 'CREATED',
     * 'VERIFIED',
     * 'CALL_REGISTERED',
     * 'IE_SCHEDULED',
     * 'INITIATE_INSPECTION',
     * 'VERIFY_PO_DETAILS',
     * 'WITHHELD'
     * )
     * )
     * )
     * 
     * ORDER BY ic.created_at DESC
     * 
     * """, nativeQuery = true)
     * List<Object[]> getInspectionCallStatusDetailsRaw(
     * 
     * @Param("stage") String stage,
     * 
     * @Param("status") String status);
     */
    @Query(value = """

            SELECT
                ic.ic_number AS inspectionCallNumber,

                COALESCE(
                    CASE 
                        WHEN ic.company_name NOT LIKE ':%' AND ic.company_name NOT REGEXP '^[0-9]+$' THEN ic.company_name 
                        ELSE NULL 
                    END,
                    SUBSTRING_INDEX(ph.vendor_details, '~', 1),
                    ic.company_name
                ) AS vendor,

                DATE_FORMAT(
                    ic.created_at,
                    '%d/%m/%Y %H:%i:%s'
                ) AS callSubmissionDateTime,

                CASE
                    WHEN ic.ic_number LIKE '%ER%' THEN 'RM Stage'
                    WHEN ic.ic_number LIKE '%EP%' THEN 'Process Stage'
                    WHEN ic.ic_number LIKE '%EF%' THEN 'Final Stage'
                    ELSE 'Other'
                END AS stageOfInspection,

                CONCAT(
                    COALESCE(ph.rly_short_name, ph.rly_cd, 'N/A'),
                    '/',
                    ic.po_no,
                    '/',
                    COALESCE(ic.po_serial_no, 'N/A')
                ) AS poSrNo,

                DATE_FORMAT(
                    pi.delivery_date,
                    '%d/%m/%Y'
                ) AS dpDate,

                CASE
                    WHEN UPPER(wt.STATUS) IN (
                        'CREATED',
                        'VERIFIED',
                        'RETURNED',
                        'CALL_REGISTERED',
                        'IE_SCHEDULED',
                        'INITIATE_INSPECTION'
                    )
                    THEN 'Pending'

                  WHEN (
                                               ic.ic_number LIKE '%EP%'
                                               AND UPPER(wt.STATUS) IN (
                                                   'PAUSED',
                                                   'PAUSE_INSPECTION_RESUME_NEXT_DAY',
                                                   'ENTER_SHIFT_DETAILS_AND_START_INSPECTION'
                                               
                                               )
                                           )
                                           OR (
                                               ic.ic_number NOT LIKE '%EP%'
                                               AND UPPER(wt.STATUS) IN (
                                                   'VERIFY_PO_DETAILS',
                                                   'PAUSED',
                                                   'PAUSE_INSPECTION_RESUME_NEXT_DAY',
                                                   'ENTER_SHIFT_DETAILS_AND_START_INSPECTION',
                                                   'WITHHELD'
                                               )
                                           )
                                           THEN 'Under Inspection'

                    ELSE 'Pending'
                END AS mainStatus,

                CASE
                    WHEN UPPER(wt.STATUS) = 'CREATED'
                        THEN 'Call Raised'

                    WHEN UPPER(wt.STATUS) = 'VERIFIED'
                        THEN 'Call Registered'

                    WHEN UPPER(wt.STATUS) = 'RETURNED'
                        THEN 'Returned to Vendor'

                    WHEN UPPER(wt.STATUS) = 'CALL_REGISTERED'
                        THEN 'Call Registered'

                    WHEN UPPER(wt.STATUS) = 'IE_SCHEDULED'
                        THEN 'Call Scheduled'

                    WHEN UPPER(wt.STATUS) IN (
                        'INITIATE_INSPECTION',
                        'VERIFY_PO_DETAILS'
                    )
                        THEN 'Inspection Started'

                    WHEN UPPER(wt.STATUS) = 'PAUSED'
                        THEN 'Paused for Next Schedule'

                    WHEN UPPER(wt.STATUS) = 'ENTER_SHIFT_DETAILS_AND_START_INSPECTION'
                        THEN 'Under Inspection'

                    WHEN UPPER(wt.STATUS) = 'WITHHELD'
                        THEN 'Withheld'

                    ELSE wt.STATUS
                END AS subStatus,

                ic.created_at

            FROM inspection_calls ic

            INNER JOIN (
                SELECT
                    REQUESTID,
                    MAX(WORKFLOWTRANSITIONID) AS latest_transition_id
                FROM workflow_transition
                GROUP BY REQUESTID
            ) latest
                ON latest.REQUESTID = ic.ic_number

            INNER JOIN workflow_transition wt
                ON wt.WORKFLOWTRANSITIONID =
                   latest.latest_transition_id

            LEFT JOIN po_header ph
                ON ph.po_no = ic.po_no

            LEFT JOIN po_item pi
                ON pi.po_header_id = ph.id
               AND pi.item_sr_no = ic.po_serial_no

            WHERE

                ic.po_no <> 'DummyPo_001'

                AND (:poiCode IS NULL OR :poiCode = '' OR ic.place_of_inspection = :poiCode)
                AND (:rlyShortName IS NULL OR :rlyShortName = '' OR ph.rly_short_name = :rlyShortName)
                AND (:startDate IS NULL OR :startDate = '' OR wt.CREATEDDATE >= :startDate)
                AND (:endDate IS NULL OR :endDate = '' OR wt.CREATEDDATE <= :endDate)

                AND UPPER(wt.STATUS) NOT IN (
                    'INSPECTION_COMPLETE_CONFIRM',
                    'WITHDRAW',
                    'CANCELLED'
                )

                AND (
                    :stage = 'ALL' OR :stage IS NULL OR :stage = ''
                    OR (:stage = 'RM' AND ic.ic_number LIKE '%ER%')
                    OR (:stage = 'Process' AND ic.ic_number LIKE '%EP%')
                    OR (:stage = 'Final' AND ic.ic_number LIKE '%EF%')
                )

                AND (
                    :status = 'ALL'

                    OR

                   (
                         :status = 'Under Inspection'
                         AND (
                             (
                                 ic.ic_number LIKE '%EP%'
                                 AND UPPER(wt.STATUS) IN (
                                     'PAUSED',
                                     'PAUSE_INSPECTION_RESUME_NEXT_DAY',
                                     'ENTER_SHIFT_DETAILS_AND_START_INSPECTION'
                                 )
                             )
                             OR
                             (
                                 ic.ic_number NOT LIKE '%EP%'
                                  AND UPPER(wt.STATUS) IN (
                                      'VERIFY_PO_DETAILS',
                                     'PAUSED',
                                     'PAUSE_INSPECTION_RESUME_NEXT_DAY',
                                     'ENTER_SHIFT_DETAILS_AND_START_INSPECTION',
                                     'WITHHELD'
                                 )
                             )
                         )
                     )

                    OR

                    (
                        :status = 'Pending'
                        AND UPPER(wt.STATUS) IN (
                            'CREATED',
                            'VERIFIED',
                            'RETURNED',
                            'CALL_REGISTERED',
                            'IE_SCHEDULED',
                            'INITIATE_INSPECTION'
                        )
                    )
                )

            ORDER BY ic.created_at DESC

            """, nativeQuery = true)
    List<Object[]> getInspectionCallStatusDetailsRaw(
            @Param("stage") String stage,
            @Param("status") String status,
            @Param("poiCode") String poiCode,
            @Param("rlyShortName") String rlyShortName,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    /*
     * @Query(value = """
     * SELECT
     * ic.ic_number AS inspectionCallNumber,
     * ic.company_name AS vendor,
     * DATE_FORMAT(ic.created_at, '%d/%m/%Y %H:%i:%s') AS callSubmissionDateTime,
     * CASE
     * WHEN ic.ic_number LIKE '%ER%' THEN 'RM Stage'
     * WHEN ic.ic_number LIKE '%EP%' THEN 'Process Stage'
     * WHEN ic.ic_number LIKE '%EF%' THEN 'Final Stage'
     * ELSE 'Other'
     * END AS stageOfInspection,
     * CONCAT(COALESCE(ph.rly_short_name, ph.rly_cd, 'N/A'), '/', ic.po_no, '/',
     * COALESCE(ic.po_serial_no, 'N/A')) AS poSrNo,
     * DATE_FORMAT(pi.delivery_date, '%d/%m/%Y') AS dpDate,
     * CASE
     * WHEN t.has_initiate = 1 THEN 'Under Inspection'
     * ELSE 'Pending'
     * END AS status
     * FROM (
     * SELECT
     * requestId,
     * MAX(CASE WHEN UPPER(status) LIKE '%INITIATE%INSPECTION%' THEN 1 ELSE 0 END)
     * AS has_initiate,
     * MAX(CASE WHEN UPPER(status) = 'CREATED' THEN 1 ELSE 0 END) AS has_created,
     * MAX(CASE WHEN UPPER(status) LIKE '%COMPLETE%CONFIRM%' THEN 1 ELSE 0 END) AS
     * is_complete
     * FROM workflow_transition
     * GROUP BY requestId
     * ) t
     * INNER JOIN inspection_calls ic ON t.requestId = ic.ic_number
     * LEFT JOIN po_header ph ON ph.po_no = ic.po_no
     * LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND pi.item_sr_no =
     * ic.po_serial_no
     * WHERE
     * t.is_complete = 0
     * AND ic.po_no <> 'DummyPo_001'
     * AND (
     * (:stage = 'RM' AND ic.ic_number LIKE '%ER%') OR
     * (:stage = 'Process' AND ic.ic_number LIKE '%EP%') OR
     * (:stage = 'Final' AND ic.ic_number LIKE '%EF%')
     * )
     * AND (
     * :status = 'ALL' OR
     * (:status = 'Under Inspection' AND t.has_initiate = 1) OR
     * (:status = 'Pending' AND t.has_initiate = 0 AND t.has_created = 1)
     * )
     * ORDER BY ic.created_at DESC
     * """, nativeQuery = true)
     * List<Object[]> getInspectionCallStatusDetailsRaw(@Param("stage") String
     * stage, @Param("status") String status);
     */
    @Query(value = """
            SELECT DISTINCT
                vm.vendor_name AS vendorName,
                ph.rly_short_name AS railwayShortName,
                ic.po_no AS poNumberOnly,
                ic.po_serial_no AS poSerialNumber,
                ic.ic_number AS callNumber,
                COALESCE(icd.certificate_no, '') AS icNumber,
                ic.type_of_call AS stage,
                DATE_FORMAT(wt.CREATEDDATE, '%Y-%m-%d') AS icIssuedDate,
                ph.item_cat_descr AS itemCatDescr,
                wt.CREATEDDATE AS rawCreatedDate
            FROM workflow_transition wt
            INNER JOIN inspection_calls ic ON wt.REQUESTID = ic.ic_number
            INNER JOIN po_header ph ON ic.po_no = ph.po_no
            LEFT JOIN vendor_master vm ON ic.vendor_id = vm.vendor_code
            LEFT JOIN inspection_complete_details icd ON ic.ic_number = icd.call_no
            WHERE wt.STATUS = 'DSC_SIGN_IC'
            AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
            AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
            AND (:startDate IS NULL OR DATE(wt.CREATEDDATE) >= :startDate)
            AND (:endDate IS NULL OR DATE(wt.CREATEDDATE) <= :endDate)
            ORDER BY rawCreatedDate DESC
            """, nativeQuery = true)
    List<Object[]> findDownloadIcAnnexuresReportRaw(
            @org.springframework.data.repository.query.Param("vendorPlantCode") String vendorPlantCode,
            @org.springframework.data.repository.query.Param("zonalRailway") String zonalRailway,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @Query("""
            SELECT wt
            FROM WorkflowTransition wt
            WHERE wt.workflowTransitionId IN (
                SELECT MAX(wt2.workflowTransitionId)
                FROM WorkflowTransition wt2
                WHERE wt2.transitionId NOT IN (42,44,45)
                  AND wt2.requestId IN (
                      SELECT ic.icNumber
                      FROM InspectionCall ic
                      WHERE ic.placeOfInspection = :poi
                  )
                GROUP BY wt2.requestId
            )
            AND wt.nextRoleName IN :roleNames
            AND wt.jobStatus IN (
                'IN_PROGRESS',
                'VERIFIED',
                'APPROVED',
                'REGISTERED',
                'Created',
                'ASSIGNED',
                'REJECTED',
                'PAUSED'
            )
            """)
    List<WorkflowTransition> findPendingByRolesAndPoi(
            @Param("poi") String poi,
            @Param("roleNames") List<String> roleNames);

    @Query("""
            SELECT wt
            FROM WorkflowTransition wt
            WHERE wt.workflowTransitionId IN (
                SELECT MAX(wt2.workflowTransitionId)
                FROM WorkflowTransition wt2
                WHERE wt2.transitionId NOT IN (42,44,45)
                  AND wt2.requestId IN (
                      SELECT ic.icNumber
                      FROM InspectionCall ic
                      WHERE ic.placeOfInspection = :poi
                  )
                GROUP BY wt2.requestId
            )
            AND wt.nextRoleName = :roleName
            AND wt.jobStatus IN (
                'IN_PROGRESS',
                'VERIFIED',
                'APPROVED',
                'REGISTERED',
                'Created',
                'ASSIGNED',
                'REJECTED',
                'PAUSED'
            )
            """)
    List<WorkflowTransition> findPendingByRoleAndPoi(
            @Param("poi") String poi,
            @Param("roleName") String roleName);

    @Query(value = """
            SELECT COUNT(*)
            FROM workflow_transition wt
            INNER JOIN inspection_calls ic ON ic.ic_number = wt.requestid
            INNER JOIN (
                SELECT requestid,
                       MAX(workflowtransitionid) latest_id
                FROM workflow_transition
                GROUP BY requestid
            ) x
            ON wt.workflowtransitionid = x.latest_id
            WHERE wt.status NOT IN (
                'INSPECTION_COMPLETE_CONFIRM',
                'GENERATE_IC',
                'DSC_SIGN_IC',
                'CANCELLED',
                'WITHDRAW'
            )
            AND ic.po_no <> 'DummyPo_001'
            """, nativeQuery = true)
    Long getTotalOpenCalls();

    @Query(value = """
            SELECT COUNT(*)
            FROM workflow_transition wt
            INNER JOIN inspection_calls ic ON ic.ic_number = wt.requestid
            INNER JOIN (
                SELECT requestid,
                       MAX(workflowtransitionid) latest_id
                FROM workflow_transition
                GROUP BY requestid
            ) x
            ON wt.workflowtransitionid = x.latest_id
            LEFT JOIN po_header ph ON ic.po_no = ph.po_no
            WHERE wt.status NOT IN (
                'INSPECTION_COMPLETE_CONFIRM',
                'GENERATE_IC',
                'DSC_SIGN_IC',
                'CANCELLED',
                'WITHDRAW'
            )
            AND ic.po_no <> 'DummyPo_001'
            AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
            AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
            AND (:startDate IS NULL OR wt.createddate >= :startDate)
            AND (:endDate IS NULL OR wt.createddate <= :endDate)
            AND (ic.ic_number LIKE '%ER%' OR ic.ic_number LIKE '%EP%' OR ic.ic_number LIKE '%EF%')
            """, nativeQuery = true)
    Long getTotalOpenCallsWithFilters(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway);

    @Query(value = """
            SELECT COUNT(*)
            FROM workflow_transition wt
            INNER JOIN inspection_calls ic ON ic.ic_number = wt.requestid
            INNER JOIN (
                SELECT requestid,
                       MAX(workflowtransitionid) latest_id
                FROM workflow_transition
                GROUP BY requestid
            ) x
              ON wt.workflowtransitionid = x.latest_id
            WHERE UPPER(wt.status) IN (
                'VERIFY_PO_DETAILS',
                'PAUSED',
                'PAUSE_INSPECTION_RESUME_NEXT_DAY',
                'ENTER_SHIFT_DETAILS_AND_START_INSPECTION'
            )
            AND ic.po_no <> 'DummyPo_001'
            """, nativeQuery = true)
    Long getTotalUnderInspectionCalls();

    @Query(value = """
            SELECT COUNT(*)
            FROM workflow_transition wt
            INNER JOIN inspection_calls ic ON ic.ic_number = wt.requestid
            INNER JOIN (
                SELECT requestid,
                       MAX(workflowtransitionid) latest_id
                FROM workflow_transition
                GROUP BY requestid
            ) x
              ON wt.workflowtransitionid = x.latest_id
            LEFT JOIN po_header ph ON ic.po_no = ph.po_no
            WHERE UPPER(wt.status) IN (
                'VERIFY_PO_DETAILS',
                'PAUSED',
                'PAUSE_INSPECTION_RESUME_NEXT_DAY',
                'ENTER_SHIFT_DETAILS_AND_START_INSPECTION'
            )
            AND ic.po_no <> 'DummyPo_001'
            AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
            AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
            AND (:startDate IS NULL OR wt.createddate >= :startDate)
            AND (:endDate IS NULL OR wt.createddate <= :endDate)
            AND (ic.ic_number LIKE '%ER%' OR ic.ic_number LIKE '%EP%' OR ic.ic_number LIKE '%EF%')
            """, nativeQuery = true)
    Long getTotalUnderInspectionCallsWithFilters(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway);

    @Query(value = """
            SELECT COUNT(*)
            FROM workflow_transition wt
            INNER JOIN inspection_calls ic ON ic.ic_number = wt.requestid
            INNER JOIN (
                SELECT requestid,
                       MAX(workflowtransitionid) latest_id
                FROM workflow_transition
                GROUP BY requestid
            ) x
            ON wt.workflowtransitionid = x.latest_id
            WHERE UPPER(wt.status) IN (
                'CREATED',
                'VERIFIED',
                'RETURNED',
                'CALL_REGISTERED',
                'IE_SCHEDULED',
                'INITIATE_INSPECTION'
            )
            AND ic.po_no <> 'DummyPo_001'
            """, nativeQuery = true)
    Long getTotalPendingCalls();

    @Query(value = """
            SELECT COUNT(*)
            FROM workflow_transition wt
            INNER JOIN inspection_calls ic ON ic.ic_number = wt.requestid
            INNER JOIN (
                SELECT requestid,
                       MAX(workflowtransitionid) latest_id
                FROM workflow_transition
                GROUP BY requestid
            ) x
            ON wt.workflowtransitionid = x.latest_id
            LEFT JOIN po_header ph ON ic.po_no = ph.po_no
            WHERE UPPER(wt.status) IN (
                'CREATED',
                'VERIFIED',
                'RETURNED',
                'CALL_REGISTERED',
                'IE_SCHEDULED',
                'INITIATE_INSPECTION'
            )
            AND ic.po_no <> 'DummyPo_001'
            AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
            AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
            AND (:startDate IS NULL OR wt.createddate >= :startDate)
            AND (:endDate IS NULL OR wt.createddate <= :endDate)
            AND (ic.ic_number LIKE '%ER%' OR ic.ic_number LIKE '%EP%' OR ic.ic_number LIKE '%EF%')
            """, nativeQuery = true)
    Long getTotalPendingCallsWithFilters(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway);

    @Query(value = """
            SELECT
                ic.ic_number AS inspectionCallNumber,
                COALESCE(vm.vendor_name, ic.vendor_id) AS vendor,
                DATE_FORMAT(ic.created_at,'%d/%m/%Y %H:%i:%s') AS callSubmissionDateTime,
                '' AS stageOfInspection,
                CONCAT(ic.po_no,'/',ic.po_serial_no) AS poSrNo,
                DATE_FORMAT(ic.desired_inspection_date,'%d/%m/%Y') AS dpDate,
                wt.status AS status
            FROM inspection_calls ic
            JOIN (
                SELECT t.*
                FROM workflow_transition t
                INNER JOIN (
                    SELECT requestid,
                           MAX(workflowtransitionid) latest_id
                    FROM workflow_transition
                    GROUP BY requestid
                ) x
                ON t.workflowtransitionid = x.latest_id
            ) wt
              ON wt.requestid = ic.ic_number
            JOIN po_header ph ON ic.po_no = ph.po_no
            LEFT JOIN vendor_master vm ON ic.vendor_id = vm.vendor_code
            WHERE wt.status NOT IN (
                'INSPECTION_COMPLETE_CONFIRM',
                'GENERATE_IC',
                'DSC_SIGN_IC',
                'CANCELLED',
                'WITHDRAW'
            )
            AND ic.po_no <> 'DummyPo_001'
            AND (:poiCode IS NULL OR :poiCode = '' OR ic.place_of_inspection = :poiCode)
            AND (:rlyShortName IS NULL OR :rlyShortName = '' OR ph.rly_short_name = :rlyShortName)
            AND (:startDate IS NULL OR wt.CREATEDDATE >= :startDate)
            AND (:endDate IS NULL OR wt.CREATEDDATE <= :endDate)
            ORDER BY ic.created_at DESC
            """, nativeQuery = true)
    List<Object[]> getOpenCalls(@Param("poiCode") String poiCode, @Param("rlyShortName") String rlyShortName,
            @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query(value = """
            SELECT
                ic.ic_number AS inspectionCallNumber,
                COALESCE(
                    CASE 
                        WHEN ic.company_name NOT LIKE ':%' AND ic.company_name NOT REGEXP '^[0-9]+$' THEN ic.company_name 
                        ELSE NULL 
                    END,
                    SUBSTRING_INDEX(ph.vendor_details, '~', 1),
                    ic.company_name,
                    ic.vendor_id
                ) AS vendor,
                DATE_FORMAT(ic.created_at,'%d/%m/%Y %H:%i:%s') AS callSubmissionDateTime,
                '' AS stageOfInspection,
                CONCAT(ic.po_no,'/',ic.po_serial_no) AS poSrNo,
                DATE_FORMAT(ic.desired_inspection_date,'%d/%m/%Y') AS dpDate,
                wt.status AS status
            FROM inspection_calls ic
            JOIN (
                SELECT t.*
                FROM workflow_transition t
                INNER JOIN (
                    SELECT requestid,
                           MAX(workflowtransitionid) latest_id
                    FROM workflow_transition
                    GROUP BY requestid
                ) x
                ON t.workflowtransitionid = x.latest_id
            ) wt
              ON wt.requestid = ic.ic_number
            JOIN po_header ph ON ic.po_no = ph.po_no
            LEFT JOIN vendor_master vm ON ic.vendor_id = vm.vendor_code
            WHERE UPPER(wt.status) IN (
                'VERIFY_PO_DETAILS',
                'PAUSED',
                'PAUSE_INSPECTION_RESUME_NEXT_DAY',
                'ENTER_SHIFT_DETAILS_AND_START_INSPECTION'
            )
            AND ic.po_no <> 'DummyPo_001'
            AND (:poiCode IS NULL OR :poiCode = '' OR ic.place_of_inspection = :poiCode)
            AND (:rlyShortName IS NULL OR :rlyShortName = '' OR ph.rly_short_name = :rlyShortName)
            AND (:startDate IS NULL OR wt.CREATEDDATE >= :startDate)
            AND (:endDate IS NULL OR wt.CREATEDDATE <= :endDate)
            ORDER BY ic.created_at DESC
            """, nativeQuery = true)
    List<Object[]> getUnderInspectionCalls(@Param("poiCode") String poiCode, @Param("rlyShortName") String rlyShortName,
            @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query(value = """
            SELECT
                ic.ic_number AS inspectionCallNumber,
                COALESCE(
                    CASE 
                        WHEN ic.company_name NOT LIKE ':%' AND ic.company_name NOT REGEXP '^[0-9]+$' THEN ic.company_name 
                        ELSE NULL 
                    END,
                    SUBSTRING_INDEX(ph.vendor_details, '~', 1),
                    ic.company_name,
                    ic.vendor_id
                ) AS vendor,
                DATE_FORMAT(ic.created_at,'%d/%m/%Y %H:%i:%s') AS callSubmissionDateTime,
                '' AS stageOfInspection,
                CONCAT(ic.po_no,'/',ic.po_serial_no) AS poSrNo,
                DATE_FORMAT(ic.desired_inspection_date,'%d/%m/%Y') AS dpDate,
                wt.status AS status
            FROM inspection_calls ic
            JOIN (
                SELECT t.*
                FROM workflow_transition t
                INNER JOIN (
                    SELECT requestid,
                           MAX(workflowtransitionid) latest_id
                    FROM workflow_transition
                    GROUP BY requestid
                ) x
                ON t.workflowtransitionid = x.latest_id
            ) wt
              ON wt.requestid = ic.ic_number
            JOIN po_header ph ON ic.po_no = ph.po_no
            WHERE UPPER(wt.status) IN (
                'CREATED',
                'VERIFIED',
                'RETURNED',
                'CALL_REGISTERED',
                'IE_SCHEDULED',
                'INITIATE_INSPECTION'
            )
            AND ic.po_no <> 'DummyPo_001'
            AND (:poiCode IS NULL OR :poiCode = '' OR ic.place_of_inspection = :poiCode)
            AND (:rlyShortName IS NULL OR :rlyShortName = '' OR ph.rly_short_name = :rlyShortName)
            AND (:startDate IS NULL OR wt.CREATEDDATE >= :startDate)
            AND (:endDate IS NULL OR wt.CREATEDDATE <= :endDate)
            ORDER BY ic.created_at DESC
            """, nativeQuery = true)
    List<Object[]> getPendingCalls(@Param("poiCode") String poiCode, @Param("rlyShortName") String rlyShortName,
            @Param("startDate") String startDate, @Param("endDate") String endDate);
}
