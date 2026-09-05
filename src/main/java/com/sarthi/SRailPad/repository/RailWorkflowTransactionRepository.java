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

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM RailWorkflowTransaction t WHERE t.requestId = :requestId AND t.moduleId = :moduleId")
    void deleteByRequestIdAndModuleId(@Param("requestId") String requestId, @Param("moduleId") Long moduleId);

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
            AND UPPER(t.status) IN ('CREATED','PENDING', 'CREATE', 'RETURNED')
            AND t.nextRole = :roleName
            AND (:workflowId IS NULL OR t.workflowId = :workflowId)
            AND (:plantId IS NULL OR :plantId = '' OR t.plantId = :plantId OR t.plantId = CONCAT(':', REPLACE(:plantId, ':', '')) OR t.plantId = REPLACE(:plantId, ':', '') OR LOWER(t.plantId) = LOWER(:plantId))
            """)
    List<RailWorkflowTransaction> findLatestByRoleAndPlantIdAndWorkflowId(@Param("roleName") String roleName,
            @Param("plantId") String plantId, @Param("workflowId") Long workflowId);

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
            AND (:plantId IS NULL OR :plantId = '' OR t.plantId = :plantId OR t.plantId = CONCAT(':', REPLACE(:plantId, ':', '')) OR t.plantId = REPLACE(:plantId, ':', '') OR LOWER(t.plantId) = LOWER(:plantId))
            """)
    List<RailWorkflowTransaction> findLatestByRoleAndPlantId(@Param("roleName") String roleName,
            @Param("plantId") String plantId);

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
            ORDER BY t.workflowTransitionId DESC
            """)
    List<RailWorkflowTransaction> findLastPendingRequestsByRole(String roleName);

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
            AND (:workflowId IS NULL OR t.workflowId = :workflowId)
            AND (:plantId IS NULL OR :plantId = '' OR t.plantId = :plantId OR t.plantId = CONCAT(':', REPLACE(:plantId, ':', '')) OR t.plantId = REPLACE(:plantId, ':', '') OR LOWER(t.plantId) = LOWER(:plantId))
            ORDER BY t.workflowTransitionId DESC
            """)
    List<RailWorkflowTransaction> findLastPendingRequestsByRoleAndPlantIdAndWorkflowId(
            @Param("roleName") String roleName, @Param("plantId") String plantId, @Param("workflowId") Long workflowId);

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
            AND (:plantId IS NULL OR :plantId = '' OR t.plantId = :plantId OR t.plantId = CONCAT(':', REPLACE(:plantId, ':', '')) OR t.plantId = REPLACE(:plantId, ':', '') OR LOWER(t.plantId) = LOWER(:plantId))
            ORDER BY t.workflowTransitionId DESC
            """)
    List<RailWorkflowTransaction> findLastPendingRequestsByRoleAndPlantId(@Param("roleName") String roleName,
            @Param("plantId") String plantId);

    List<RailWorkflowTransaction> findByRequestIdOrderByCreatedDateAsc(String requestId);

    @Query(value = """
            SELECT t.* FROM rail_workflow_transaction t
            WHERE t.workflow_transition_id IN (
                SELECT MAX(wt.workflow_transition_id)
                FROM rail_workflow_transaction wt
                GROUP BY wt.request_id
            )
            AND (
                t.action IN :pendingActions
                OR UPPER(t.status) = 'PENDING'
                OR UPPER(t.status) = 'RIO_VERIFIED'
                OR UPPER(COALESCE(t.job_status, '')) IN ('RIO_VERIFIED', 'SCHEDULED', 'ASSIGNED', 'IN_PROGRESS', 'INITIATED', 'PAUSED', 'RESUMED')
            )
            AND UPPER(t.status) NOT IN ('CREATED', 'RESUBMITTED')
            AND UPPER(t.status) NOT LIKE '%CANCEL%'
            AND UPPER(COALESCE(t.job_status, '')) NOT LIKE '%CANCEL%'
            AND t.action NOT IN ('DSC_SIGN_IC', 'SIGN_IC')
            """, nativeQuery = true)
    List<RailWorkflowTransaction> findPendingVerifiedCalls(@Param("pendingActions") List<String> pendingActions);

    @Query(value = """
            SELECT wt.request_id, wt.rio 
            FROM rail_workflow_transaction wt
            WHERE wt.workflow_transition_id IN (
                SELECT MIN(wt2.workflow_transition_id)
                FROM rail_workflow_transaction wt2
                WHERE wt2.request_id IN :requestIds
                  AND wt2.rio IS NOT NULL AND wt2.rio != ''
                GROUP BY wt2.request_id
            )
            """, nativeQuery = true)
    List<Object[]> findInitialRiosByRequestIds(@Param("requestIds") List<String> requestIds);

    @Query("""
            SELECT t FROM RailWorkflowTransaction t
            WHERE t.workflowTransitionId = (
                SELECT MAX(t2.workflowTransitionId)
                FROM RailWorkflowTransaction t2
                WHERE t2.requestId = t.requestId
                AND COALESCE(t2.moduleId, 0) = COALESCE(t.moduleId, 0)
            )
            AND (UPPER(t.status) = 'COMPLETED' OR UPPER(t.status) LIKE '%CANCEL%' OR UPPER(COALESCE(t.jobStatus, '')) LIKE '%CANCEL%' OR UPPER(COALESCE(t.action, '')) LIKE '%CANCEL%')
            """)
    List<RailWorkflowTransaction> findCompletedRequests();

    @Query("""
            SELECT t FROM RailWorkflowTransaction t
            WHERE t.workflowTransitionId = (
                SELECT MAX(t2.workflowTransitionId)
                FROM RailWorkflowTransaction t2
                WHERE t2.requestId = t.requestId
                AND COALESCE(t2.moduleId, 0) = COALESCE(t.moduleId, 0)
            )
            AND (UPPER(t.status) = 'COMPLETED' OR UPPER(t.status) LIKE '%CANCEL%' OR UPPER(COALESCE(t.jobStatus, '')) LIKE '%CANCEL%' OR UPPER(COALESCE(t.action, '')) LIKE '%CANCEL%')
            AND (:workflowId IS NULL OR t.workflowId = :workflowId)
            AND (:plantId IS NULL OR :plantId = '' OR t.plantId = :plantId OR t.plantId = CONCAT(':', REPLACE(:plantId, ':', '')) OR t.plantId = REPLACE(:plantId, ':', '') OR LOWER(t.plantId) = LOWER(:plantId))
            """)
    List<RailWorkflowTransaction> findCompletedRequestsByPlantIdAndWorkflowId(@Param("plantId") String plantId,
            @Param("workflowId") Long workflowId);

    @Query("""
            SELECT t FROM RailWorkflowTransaction t
            WHERE t.workflowTransitionId = (
                SELECT MAX(t2.workflowTransitionId)
                FROM RailWorkflowTransaction t2
                WHERE t2.requestId = t.requestId
                AND COALESCE(t2.moduleId, 0) = COALESCE(t.moduleId, 0)
            )
            AND (UPPER(t.status) = 'COMPLETED' OR UPPER(t.status) LIKE '%CANCEL%' OR UPPER(COALESCE(t.jobStatus, '')) LIKE '%CANCEL%' OR UPPER(COALESCE(t.action, '')) LIKE '%CANCEL%')
            AND (:plantId IS NULL OR :plantId = '' OR t.plantId = :plantId OR t.plantId = CONCAT(':', REPLACE(:plantId, ':', '')) OR t.plantId = REPLACE(:plantId, ':', '') OR LOWER(t.plantId) = LOWER(:plantId))
            """)
    List<RailWorkflowTransaction> findCompletedRequestsByPlantId(@Param("plantId") String plantId);

    @Query("""
            SELECT t FROM RailWorkflowTransaction t
            WHERE t.workflowTransitionId = (
                SELECT MAX(t2.workflowTransitionId)
                FROM RailWorkflowTransaction t2
                WHERE t2.requestId = t.requestId
                AND (t2.moduleId = t.moduleId OR (t2.moduleId IS NULL AND t.moduleId IS NULL))
                AND t2.workflowId = 2
                AND (UPPER(t2.status) = 'COMPLETED' OR UPPER(t2.status) LIKE '%CANCEL%' OR UPPER(COALESCE(t2.jobStatus, '')) LIKE '%CANCEL%' OR UPPER(COALESCE(t2.action, '')) LIKE '%CANCEL%')
            )
            AND (UPPER(t.status) = 'COMPLETED' OR UPPER(t.status) LIKE '%CANCEL%' OR UPPER(COALESCE(t.jobStatus, '')) LIKE '%CANCEL%' OR UPPER(COALESCE(t.action, '')) LIKE '%CANCEL%')
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
            @Param("moduleId") Long moduleId);

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
            @Param("plantId") String plantId);

    @Query(value = "SELECT poi_code FROM rail_workflow_transaction WHERE request_id = :requestId ORDER BY workflow_transition_id DESC LIMIT 1", nativeQuery = true)
    String findLatestPoiByRequestId(@Param("requestId") String requestId);

    RailWorkflowTransaction findFirstByRequestIdOrderByWorkflowTransitionIdDesc(String requestId);

    @Query(value = "SELECT rio FROM rail_workflow_transaction WHERE request_id = :requestId AND rio IS NOT NULL ORDER BY workflow_transition_id ASC LIMIT 1", nativeQuery = true)
    String findRioByRequestId(@Param("requestId") String requestId);

    @Query(value = "SELECT status FROM rail_workflow_transaction WHERE request_id = :requestId ORDER BY workflow_transition_id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestStatusByRequestId(@Param("requestId") String requestId);

    @Query(value = "SELECT status FROM rail_workflow_transaction WHERE request_id = :requestId AND (UPPER(status) LIKE '%CANCEL%' OR UPPER(COALESCE(job_status, '')) LIKE '%CANCEL%' OR UPPER(COALESCE(action, '')) LIKE '%CANCEL%') ORDER BY workflow_transition_id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLatestCancelledStatusByRequestId(@Param("requestId") String requestId);

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
                    COALESCE(SUM(CASE WHEN t.has_initiate = 1 THEN 1 ELSE 0 END), 0) as under_inspection,
                    COALESCE(SUM(CASE WHEN t.has_initiate = 0 THEN 1 ELSE 0 END), 0) as pending
                FROM (
                    SELECT
                        rwt1.request_id,
                        CASE WHEN EXISTS (
                            SELECT 1
                            FROM rail_workflow_transaction rwt2
                            WHERE rwt2.request_id = rwt1.request_id
                              AND (UPPER(rwt2.action) = 'INITIATE_CALL' OR UPPER(rwt2.job_status) = 'INITIATED')
                        ) THEN 1 ELSE 0 END as has_initiate
                    FROM rail_workflow_transaction rwt1
                    LEFT JOIN rail_inspection_call ic ON CONVERT(rwt1.request_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                    LEFT JOIN po_header p ON CONVERT(
                        CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END 
                        USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(p.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                    WHERE rwt1.workflow_transition_id IN (
                          SELECT MAX(rwt3.workflow_transition_id)
                          FROM rail_workflow_transaction rwt3
                          GROUP BY rwt3.request_id
                      )
                      AND NOT EXISTS (
                          SELECT 1
                          FROM rail_workflow_transaction rwt4
                          WHERE rwt4.request_id = rwt1.request_id
                            AND (UPPER(rwt4.status) = 'COMPLETED' OR UPPER(rwt4.job_status) = 'COMPLETED')
                      )
                      AND (:vCode IS NULL OR :vCode = '' OR 
                           CONVERT(ic.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
                           CONVERT(ic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
                           CONVERT(ic.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (
                               SELECT CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci 
                               FROM rail_vendor_plant rvp 
                               WHERE CONVERT(rvp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci 
                                  OR CONVERT(rvp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci 
                                  OR CONVERT(rvp.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
                           ) OR
                           CONVERT(ic.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (
                               SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci 
                               FROM railpad_pincode_poi_mapping ppm 
                               WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci 
                                  OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
                           ) OR
                           CONVERT(ic.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci IN (
                               SELECT CONVERT(ppm.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci 
                               FROM pincode_poi_mapping ppm 
                               WHERE CONVERT(ppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci 
                                  OR CONVERT(ppm.company_name USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(:vCode USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
                           )
                          )
                      AND (:zCode IS NULL OR :zCode = '' OR 
                           CONVERT(p.rly_short_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR 
                           CONVERT(p.rly_cd USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(:zCode USING utf8mb4) COLLATE utf8mb4_unicode_ci OR
                           (p.id IS NULL AND (:vCode IS NOT NULL AND :vCode <> ''))
                          )
                      AND (:startDate IS NULL OR DATE(ic.created_at) >= :startDate)
                      AND (:endDate IS NULL OR DATE(ic.created_at) <= :endDate)
                ) t
            """, nativeQuery = true)
    List<Object[]> getFilteredRailPadInspectionCallCounts(
            @org.springframework.data.repository.query.Param("vCode") String vCode,
            @org.springframework.data.repository.query.Param("zCode") String zCode,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate
    );

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

    @Query(value = """
                SELECT
                    stage,
                    SUM(CASE WHEN action IN ('CREATED', 'CREATE', 'VERIFY', 'MAIN_IE_SCHEDULE_CALL', 'INITIATE_CALL') THEN 1 ELSE 0 END) AS pending_calls,
                    SUM(CASE WHEN action IN ('PO_VERIFICATION', 'PAUSE', 'RESUME') THEN 1 ELSE 0 END) AS under_inspection_calls,
                    SUM(CASE WHEN action IN ('FINISH', 'COMPLETED', 'IC_GENERATION', 'GENERATE_IC', 'DSC_SIGN_IC') THEN 1 ELSE 0 END) AS completed_calls,
                    SUM(CASE WHEN action IN ('GENERATE_IC', 'DSC_SIGN_IC', 'IC_GENERATION') THEN 1 ELSE 0 END) AS ic_issued_calls
                FROM (
                    SELECT
                        rwt.request_id,
                        rwt.action,
                        rwt.vendor_code,
                        rwt.plant_id,
                        rwt.poi_code,
                        rwt.created_date,
                        CASE
                            WHEN rwt.request_id LIKE 'RPP%' THEN 'Process'
                            WHEN rwt.request_id LIKE 'RPF%' THEN 'Final'
                            ELSE 'Other'
                        END AS stage
                    FROM rail_workflow_transaction rwt
                    INNER JOIN (
                        SELECT request_id, MAX(workflow_transition_id) AS max_id
                        FROM rail_workflow_transaction
                        WHERE workflow_id = 2
                        GROUP BY request_id
                    ) latest ON rwt.request_id = latest.request_id AND rwt.workflow_transition_id = latest.max_id
                    INNER JOIN rail_inspection_call ic ON rwt.request_id COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
                    LEFT JOIN po_header ph ON ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(ic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci
                    WHERE (rwt.request_id LIKE 'RPP%' OR rwt.request_id LIKE 'RPF%')
                    AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR
                           rwt.plant_id = :vendorPlantCode OR
                           CONCAT(':', rwt.plant_id) = :vendorPlantCode OR
                           rwt.plant_id = REPLACE(:vendorPlantCode, ':', '') OR
                           rwt.vendor_code = :vendorPlantCode OR
                           CONCAT(':', rwt.vendor_code) = :vendorPlantCode OR
                           rwt.vendor_code = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR
                           CONCAT(':', rwt.vendor_code) = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR
                           rwt.vendor_code = REPLACE(SUBSTRING_INDEX(:vendorPlantCode, '/', 1), ':', '') OR
                           rwt.poi_code = :vendorPlantCode
                    )
                    AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
                    AND (:startDate IS NULL OR :endDate IS NULL OR ic.created_at BETWEEN :startDate AND :endDate)
                ) t
                GROUP BY stage
            """, nativeQuery = true)
    List<Object[]> getRailPadStagewiseCallCountsRaw(
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
                SELECT
                    ic.call_no AS inspectionCallNumber,
                    COALESCE(vm.vendor_name, ic.vendor_code) AS vendor,
                    DATE_FORMAT(ic.created_at, '%d/%m/%Y %H:%i:%s') AS callSubmissionDateTime,
                    CASE
                        WHEN t.request_id LIKE 'RPP%' THEN 'Process'
                        WHEN t.request_id LIKE 'RPF%' THEN 'Final Product'
                        ELSE 'Railpad'
                    END AS stageOfInspection,
                    CONCAT(COALESCE(ph.rly_cd, 'N/A'), ' / ', ic.po_no) AS poSrNo,
                    DATE_FORMAT(pi.delivery_date, '%d/%m/%Y') AS dpDate,
                    CASE
                        WHEN t.action IN ('PO_VERIFICATION', 'PAUSE', 'RESUME') THEN 'Under Inspection'
                        WHEN t.action IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'IC_GENERATION') THEN 'Completed'
                        ELSE 'Pending'
                    END AS status
                FROM (
                    SELECT
                        rwt1.request_id,
                        rwt1.action,
                        rwt1.plant_id,
                        rwt1.vendor_code,
                        rwt1.poi_code
                    FROM rail_workflow_transaction rwt1
                    INNER JOIN (
                        SELECT request_id, MAX(workflow_transition_id) AS max_id
                        FROM rail_workflow_transaction
                        WHERE workflow_id = 2
                        GROUP BY request_id
                    ) latest ON rwt1.request_id = latest.request_id AND rwt1.workflow_transition_id = latest.max_id
                    WHERE (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR
                           rwt1.plant_id = :vendorPlantCode OR
                           CONCAT(':', rwt1.plant_id) = :vendorPlantCode OR
                           rwt1.plant_id = REPLACE(:vendorPlantCode, ':', '') OR
                           rwt1.vendor_code = :vendorPlantCode OR
                           CONCAT(':', rwt1.vendor_code) = :vendorPlantCode OR
                           rwt1.vendor_code = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR
                           CONCAT(':', rwt1.vendor_code) = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR
                           rwt1.vendor_code = REPLACE(SUBSTRING_INDEX(:vendorPlantCode, '/', 1), ':', '') OR
                           rwt1.poi_code = :vendorPlantCode
                    )
                ) t
                INNER JOIN rail_inspection_call ic ON t.request_id COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
                LEFT JOIN vendor_master vm ON vm.vendor_code COLLATE utf8mb4_unicode_ci = ic.vendor_code COLLATE utf8mb4_unicode_ci
                LEFT JOIN po_header ph ON ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(ic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci
                LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND pi.item_sr_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(ic.po_no, '/', -1) COLLATE utf8mb4_unicode_ci
                WHERE (:stage = 'ALL' OR
                       (:stage = 'Process' AND t.request_id LIKE 'RPP%') OR
                       (:stage = 'Final' AND t.request_id LIKE 'RPF%') OR
                       (:stage = 'RM' AND t.request_id LIKE 'RPRM%'))
                  AND (:status = 'ALL' OR
                       (:status = 'Open' AND t.action IN ('CREATED', 'CREATE', 'VERIFY', 'MAIN_IE_SCHEDULE_CALL', 'INITIATE_CALL', 'PO_VERIFICATION', 'PAUSE', 'RESUME')) OR
                       (:status = 'Under Inspection' AND t.action IN ('PO_VERIFICATION', 'PAUSE', 'RESUME')) OR
                       (:status = 'Pending' AND t.action IN ('CREATED', 'CREATE', 'VERIFY', 'MAIN_IE_SCHEDULE_CALL', 'INITIATE_CALL')) OR
                       (:status = 'IC Issued' AND t.action IN ('FINISH', 'COMPLETED', 'IC_ISSUE', 'IC_GENERATION')))
                  AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
                  AND (:startDate IS NULL OR :endDate IS NULL OR ic.created_at BETWEEN :startDate AND :endDate)
                ORDER BY ic.created_at DESC
            """, nativeQuery = true)
    List<Object[]> getRailPadInspectionCallStatusDetailsFiltered(
            @Param("stage") String stage,
            @Param("status") String status,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    @Query(value = """
        SELECT 
            COALESCE(SUM(pir.total_accepted_qty), 0) AS process_accepted_nos,
            COALESCE(SUM(pir.total_rejected_qty), 0) AS process_rejected_nos
        FROM rail_process_inspection_result pir
        JOIN rail_inspection_call ic ON pir.inspection_call_id = ic.id
        LEFT JOIN po_header ph ON ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(ic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci
        WHERE (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR 
               ic.vendor_code = :vendorPlantCode OR 
               ic.vendor_code = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR 
               ic.vendor_code = REPLACE(SUBSTRING_INDEX(:vendorPlantCode, '/', 1), ':', '')
        )
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
        AND (:startDate IS NULL OR :endDate IS NULL OR ic.created_at BETWEEN :startDate AND :endDate)
    """, nativeQuery = true)
    List<Object[]> getRailPadProcessInspectionSummary(
        @Param("vendorPlantCode") String vendorPlantCode,
        @Param("zonalRailway") String zonalRailway,
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );

    @Query(value = """
        SELECT 
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) != 'set' THEN flr.accepted_qty ELSE 0 END), 0) AS final_accepted_nos,
            COALESCE(SUM(CASE WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) = 'set' THEN flr.accepted_qty ELSE 0 END), 0) AS final_accepted_set,
            COALESCE(SUM(CASE 
                WHEN (flr.railpad_type IS NULL OR UPPER(flr.railpad_type) NOT LIKE '%NCRGRSP%') 
                     AND LOWER(TRIM(COALESCE(pi.uom, ''))) != 'set' 
                THEN flr.rejected_qty 
                ELSE 0 
            END), 0) AS final_rejected_nos,
            COALESCE(SUM(CASE 
                WHEN UPPER(COALESCE(flr.railpad_type, '')) LIKE '%NCRGRSP%' THEN
                    CASE 
                        WHEN flr.id = first_flr.first_lot_id THEN flr.rejected_qty 
                        ELSE 0 
                    END
                WHEN LOWER(TRIM(COALESCE(pi.uom, ''))) = 'set' 
                THEN flr.rejected_qty 
                ELSE 0 
            END), 0) AS final_rejected_set
        FROM rail_final_inspection_lot_results flr
        LEFT JOIN (
            SELECT call_no, 
                   CAST(SUBSTRING_INDEX(GROUP_CONCAT(id ORDER BY 
                       CASE 
                           WHEN lot_no REGEXP '[0-9]+' THEN CAST(REGEXP_SUBSTR(lot_no, '[0-9]+') AS UNSIGNED)
                           ELSE 999999 
                       END ASC, 
                       id ASC
                   ), ',', 1) AS UNSIGNED) AS first_lot_id
            FROM rail_final_inspection_lot_results
            GROUP BY call_no
        ) first_flr ON flr.call_no COLLATE utf8mb4_unicode_ci = first_flr.call_no COLLATE utf8mb4_unicode_ci
        LEFT JOIN rail_inspection_call ic ON flr.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_header ph ON ph.po_no COLLATE utf8mb4_unicode_ci = (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(TRIM(SUBSTRING_INDEX(ic.po_no, '/', 1)), ' ', -1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci
        LEFT JOIN po_item pi ON pi.po_header_id = ph.id AND (
            pi.item_sr_no COLLATE utf8mb4_unicode_ci = (CASE WHEN ic.po_no LIKE '%/%' THEN TRIM(SUBSTRING_INDEX(ic.po_no, '/', -1)) ELSE ic.po_sr END) COLLATE utf8mb4_unicode_ci 
            OR CAST(pi.item_sr_no AS UNSIGNED) = CAST((CASE WHEN ic.po_no LIKE '%/%' THEN TRIM(SUBSTRING_INDEX(ic.po_no, '/', -1)) ELSE ic.po_sr END) AS UNSIGNED)
        )
        WHERE (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR 
               flr.plant_id = :vendorPlantCode OR 
               flr.vendor_code = :vendorPlantCode OR 
               flr.vendor_code = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR 
               flr.vendor_code = REPLACE(SUBSTRING_INDEX(:vendorPlantCode, '/', 1), ':', '')
        )
        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
        AND (:startDate IS NULL OR :endDate IS NULL OR flr.created_date BETWEEN :startDate AND :endDate)
    """, nativeQuery = true)
    List<Object[]> getRailPadFinalInspectionSummary(
        @Param("vendorPlantCode") String vendorPlantCode,
        @Param("zonalRailway") String zonalRailway,
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );

    @Query(value = """
            SELECT DISTINCT
                COALESCE(
                    CASE 
                        WHEN vm.vendor_name IS NOT NULL AND vm.vendor_name != '' AND vm.vendor_name NOT REGEXP '^[0-9]+$' 
                        THEN vm.vendor_name 
                        ELSE NULL 
                    END,
                    NULLIF(SUBSTRING_INDEX(ph.vendor_details, '~', 1), ''),
                    NULLIF(ph.firm_details, ''),
                    ic.vendor_code,
                    ''
                ) AS vendorName,
                COALESCE(ph.rly_short_name, '') AS railwayShortName,
                SUBSTRING_INDEX(ic.po_no, '/', 1) AS poNumberOnly,
                CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE COALESCE(ic.po_sr, '') END AS poSerialNumber,
                ic.call_no AS callNumber,
                COALESCE(ricd.certificate_no, ic.call_no) AS icNumber,
                CASE
                    WHEN rwt.request_id LIKE 'RPP%' THEN 'Process'
                    WHEN rwt.request_id LIKE 'RPF%' THEN 'Final'
                    WHEN rwt.request_id LIKE 'RPRM%' THEN 'RM'
                    ELSE 'Process'
                END AS stage,
                DATE_FORMAT(rwt.created_date, '%Y-%m-%d') AS icIssuedDate,
                COALESCE(ph.item_cat_descr, 'Rail Pad') AS itemCatDescr,
                rwt.created_date AS rawCreatedDate
            FROM rail_workflow_transaction rwt
            INNER JOIN (
                SELECT request_id, MAX(workflow_transition_id) AS max_id
                FROM rail_workflow_transaction
                WHERE workflow_id IN (1, 2)
                  AND action IN ('GENERATE_IC', 'DSC_SIGN_IC', 'IC_GENERATION')
                GROUP BY request_id
            ) latest ON rwt.request_id = latest.request_id AND rwt.workflow_transition_id = latest.max_id
            INNER JOIN rail_inspection_call ic ON rwt.request_id COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_header ph ON ph.po_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(ic.po_no, '/', 1) COLLATE utf8mb4_unicode_ci
            LEFT JOIN vendor_master vm ON ic.vendor_code COLLATE utf8mb4_unicode_ci = vm.vendor_code COLLATE utf8mb4_unicode_ci
            LEFT JOIN rail_inspection_complete_details ricd ON ic.call_no COLLATE utf8mb4_unicode_ci = ricd.call_no COLLATE utf8mb4_unicode_ci
            WHERE (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR
                   rwt.plant_id = :vendorPlantCode OR
                   CONCAT(':', rwt.plant_id) = :vendorPlantCode OR
                   rwt.plant_id = REPLACE(:vendorPlantCode, ':', '') OR
                   rwt.vendor_code = :vendorPlantCode OR
                   CONCAT(':', rwt.vendor_code) = :vendorPlantCode OR
                   rwt.vendor_code = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR
                   CONCAT(':', rwt.vendor_code) = SUBSTRING_INDEX(:vendorPlantCode, '/', 1) OR
                   rwt.vendor_code = REPLACE(SUBSTRING_INDEX(:vendorPlantCode, '/', 1), ':', '') OR
                   rwt.poi_code = :vendorPlantCode
            )
            AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
            AND (:startDate IS NULL OR DATE(rwt.created_date) >= :startDate)
            AND (:endDate IS NULL OR DATE(rwt.created_date) <= :endDate)
            ORDER BY rawCreatedDate DESC
            """, nativeQuery = true)
    List<Object[]> findRailPadDownloadIcAnnexuresReportRaw(
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);

    @Query(value = "SELECT rio FROM rail_workflow_transaction WHERE (request_id = :callNo OR request_id LIKE CONCAT('%', :callNo, '%')) AND rio IS NOT NULL AND rio != '' ORDER BY workflow_transition_id ASC LIMIT 1", nativeQuery = true)
    String findRioByCallNo(@Param("callNo") String callNo);

    @Query(value = """
            SELECT t.* FROM rail_workflow_transaction t
            WHERE t.workflow_transition_id IN (
                SELECT MAX(t2.workflow_transition_id)
                FROM rail_workflow_transaction t2
                WHERE (UPPER(t2.status) LIKE '%CANCEL%' OR UPPER(COALESCE(t2.job_status, '')) LIKE '%CANCEL%')
                GROUP BY t2.request_id
            )
            AND (:plantId IS NULL OR :plantId = '' 
                 OR REPLACE(t.plant_id, ':', '') = :plantId)
            AND (:vendorCode IS NULL OR :vendorCode = '' 
                 OR REPLACE(t.vendor_code, ':', '') = :vendorCode 
                 OR REPLACE(t.plant_id, ':', '') LIKE CONCAT(:vendorCode, '%'))
            ORDER BY t.created_date DESC
            """, nativeQuery = true)
    List<RailWorkflowTransaction> findLatestCancelledTransactions(@Param("plantId") String plantId, @Param("vendorCode") String vendorCode);
}
