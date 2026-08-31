package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailInspectionCallRepository extends JpaRepository<RailInspectionCall, Long> {
    
    @Query(value = "SELECT call_no FROM rail_inspection_call WHERE call_no LIKE ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastCallNoByPattern(String pattern);

    @Query("SELECT SUM(c.totalQty) FROM RailInspectionCall c WHERE c.poNo = :poNo")
    Integer findTotalQtyByPoNo(String poNo);

    long countByPoNo(String poNo);

    List<RailInspectionCall> findAllByVendorCode(String vendorCode);

    List<RailInspectionCall> findAllByPlantId(String plantId);

    Page<RailInspectionCall> findByVendorCodeOrderByCreatedAtDesc(String vendorCode, Pageable pageable);

    Page<RailInspectionCall> findByPlantIdOrderByCreatedAtDesc(String plantId, Pageable pageable);

    @Query(value = """
            SELECT c.* FROM rail_inspection_call c 
            WHERE REPLACE(c.plant_id, ':', '') = REPLACE(:plantId, ':', '') 
            AND UPPER(COALESCE((SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1), c.status, 'PENDING')) NOT IN (:statuses)
            AND UPPER(COALESCE((SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1), c.status, 'PENDING')) NOT LIKE '%CANCEL%'
            ORDER BY c.created_at DESC
            """,
           countQuery = """
            SELECT count(c.id) FROM rail_inspection_call c 
            WHERE REPLACE(c.plant_id, ':', '') = REPLACE(:plantId, ':', '') 
            AND UPPER(COALESCE((SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1), c.status, 'PENDING')) NOT IN (:statuses)
            AND UPPER(COALESCE((SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1), c.status, 'PENDING')) NOT LIKE '%CANCEL%'
            """,
           nativeQuery = true)
    Page<RailInspectionCall> findPendingCallsForPlantNative(@Param("plantId") String plantId, @Param("statuses") List<String> statuses, Pageable pageable);
    
    @Query(value = """
            SELECT c.* FROM rail_inspection_call c 
            WHERE REPLACE(c.plant_id, ':', '') = REPLACE(:plantId, ':', '') 
            AND (
                UPPER(COALESCE((SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1), c.status, 'PENDING')) IN (:statuses)
                OR UPPER(COALESCE((SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1), c.status, 'PENDING')) LIKE '%CANCEL%'
            )
            ORDER BY c.created_at DESC
            """,
           countQuery = """
            SELECT count(c.id) FROM rail_inspection_call c 
            WHERE REPLACE(c.plant_id, ':', '') = REPLACE(:plantId, ':', '') 
            AND (
                UPPER(COALESCE((SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1), c.status, 'PENDING')) IN (:statuses)
                OR UPPER(COALESCE((SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1), c.status, 'PENDING')) LIKE '%CANCEL%'
            )
            """,
           nativeQuery = true)
    Page<RailInspectionCall> findCompletedCallsForPlantNative(@Param("plantId") String plantId, @Param("statuses") List<String> statuses, Pageable pageable);


    Optional<RailInspectionCall> findByCallNo(String callNo);
    List<RailInspectionCall> findByCallNoIn(List<String> callNos);

    @Query(value = """
        SELECT COUNT(DISTINCT ic.call_no)
        FROM rail_inspection_call ic
        WHERE 
            (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci = :poNo COLLATE utf8mb4_unicode_ci
            AND (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE ic.po_sr END) COLLATE utf8mb4_unicode_ci = :poSr COLLATE utf8mb4_unicode_ci
    """, nativeQuery = true)
    Long countCallsByPoAndSr(@org.springframework.data.repository.query.Param("poNo") String poNo, @org.springframework.data.repository.query.Param("poSr") String poSr);

    @Query(value = """
        SELECT 
            CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END AS poNo,
            GROUP_CONCAT(DISTINCT ic.rail_pad_type SEPARATOR ', ') AS railpadTypes
        FROM rail_inspection_call ic
        WHERE ic.rail_pad_type IS NOT NULL AND ic.rail_pad_type <> ''
        GROUP BY 
            CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END
    """, nativeQuery = true)
    List<Object[]> findDistinctRailpadTypesGroupByPo();

    @Query(value = """
        SELECT COALESCE(SUM(ic.total_qty), 0)
        FROM rail_inspection_call ic
        WHERE 
            (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) = :poNo
            AND (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', -1) ELSE ic.po_sr END) = :poSr
            AND ic.call_type = 'FINAL'
            AND ic.created_at < :createdAt
    """, nativeQuery = true)
    Double sumTotalQtyByPoAndSrBeforeDate(@org.springframework.data.repository.query.Param("poNo") String poNo, @org.springframework.data.repository.query.Param("poSr") String poSr, @org.springframework.data.repository.query.Param("createdAt") java.time.LocalDateTime createdAt);

    @Query("""
        SELECT DISTINCT c FROM RailInspectionCall c 
        LEFT JOIN RailProcessCallDetails d ON d.inspectionCall.id = c.id 
        WHERE c.callType = 'PROCESS' 
          AND (:plantId IS NULL OR :plantId = '' OR c.plantId = :plantId) 
          AND (:railPadType IS NULL OR :railPadType = '' OR c.railPadType = :railPadType OR :railPadType LIKE '%NCRGRSP%' OR c.railPadType LIKE '%NCRGRSP%') 
          AND (:drawingNo IS NULL OR :drawingNo = '' OR d.drawingNo = :drawingNo)
          AND (:poNo IS NULL OR :poNo = '' OR c.poNo = :poNo OR c.poNo LIKE CONCAT(:poNo, '/%'))
          AND (:poSr IS NULL OR :poSr = '' OR c.poSr = :poSr OR LTRIM(RTRIM(c.poSr)) = LTRIM(RTRIM(:poSr)))
    """)
    List<RailInspectionCall> findProcessCalls(
        @Param("railPadType") String railPadType, 
        @Param("drawingNo") String drawingNo, 
        @Param("plantId") String plantId,
        @Param("poNo") String poNo,
        @Param("poSr") String poSr
    );

    @Query("SELECT c FROM RailInspectionCall c JOIN RailProcessCallDetails d ON d.inspectionCall.id = c.id WHERE c.callType = 'PROCESS' AND c.railPadType = :railPadType AND d.drawingNo = :drawingNo AND c.plantId = :plantId")
    List<RailInspectionCall> findProcessCallsByTypeAndDrawingAndPlant(@Param("railPadType") String railPadType, @Param("drawingNo") String drawingNo, @Param("plantId") String plantId);

    @Query(value = """
            SELECT
                COALESCE(ph.case_no, '')                                AS caseNumber,
                DATE(ic.created_at)                                     AS callDate,
                COALESCE(CONVERT(rpp.poi_code USING utf8mb4), CONVERT(ic.plant_id USING utf8mb4)) AS placeOfInspection,
                COALESCE(CONVERT(pm.ibs_vendor_code USING utf8mb4), CONVERT(ic.plant_id USING utf8mb4)) AS ibsManufacturedCode,
                CAST(COALESCE(um.employee_code, cd.created_by, wt.created_by, ic.created_by) AS CHAR) AS ieEmployeeNumber,
                'C'                                                     AS callStatus,
                (CASE WHEN ic.call_type = 'PROCESS' THEN 'P' ELSE 'F' END) AS typeOfCall,
                (CASE 
                    WHEN ic.po_no LIKE '%/%' AND SUBSTRING_INDEX(ic.po_no, '/', -1) <> '' THEN SUBSTRING_INDEX(ic.po_no, '/', -1)
                    WHEN ic.po_sr IS NOT NULL AND TRIM(ic.po_sr) <> '' THEN TRIM(ic.po_sr)
                    ELSE '1'
                END)                                                    AS poItemSerialNumber,
                ''                                                      AS bkNumber,
                ''                                                      AS setNumber,
                DATE(COALESCE(cd.created_date, wt.created_date, ic.updated_at, ic.created_at)) AS icDate,
                COALESCE(ic.total_qty, 0)                               AS quantityOffered,
                0                                                       AS quantityPassed,
                0                                                       AS quantityRejected,
                ic.call_no                                              AS callNo,
                ic.call_no                                              AS callNumber,
                COALESCE(
                    (CASE WHEN cd.cancellation_basis = 'CHARGEABLE' THEN COALESCE(cd.final_cancellation_charges, cd.calculated_charges, 0) ELSE 0 END),
                    (CASE WHEN vfl_c.liability_type = 'CANCELLATION_CHARGES' THEN COALESCE(vfl_c.amount, 0) ELSE 0 END),
                    0
                )                                                       AS cancellationCharges,
                0.0                                                     AS rejectionCharges
            FROM rail_inspection_call ic
            LEFT JOIN rail_call_cancellation_details cd
                   ON CONVERT(cd.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN rail_workflow_transaction wt
                   ON wt.workflow_transition_id = (
                       SELECT MAX(wt2.workflow_transition_id)
                       FROM rail_workflow_transaction wt2
                       WHERE wt2.request_id = ic.call_no
                         AND (UPPER(wt2.status) LIKE '%CANCEL%' OR UPPER(COALESCE(wt2.job_status, '')) LIKE '%CANCEL%')
                   )
            LEFT JOIN po_header ph
                   ON CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT((CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um
                   ON CONVERT(um.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(cd.created_by, wt.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(cd.created_by, wt.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN railpad_pincode_poi_mapping rpp
                   ON CONVERT(REPLACE(TRIM(rpp.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT(SUBSTRING_INDEX(TRIM(ic.plant_id), '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(rpp.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT(REPLACE(TRIM(COALESCE(ic.vendor_code, '')), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN sarthi_ibs_poi_mapping pm
                   ON CONVERT(pm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(rpp.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci
                  AND pm.product_type = 'railpad'
            LEFT JOIN rail_vendor_financial_liability vfl_c
                   ON CONVERT(vfl_c.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                  AND vfl_c.liability_type = 'CANCELLATION_CHARGES'
            LEFT JOIN (
                SELECT icr1.*
                FROM ibs_call_registration icr1
                INNER JOIN (
                    SELECT call_number, MAX(version) AS max_version
                    FROM ibs_call_registration
                    GROUP BY call_number
                ) latest
                    ON CONVERT(latest.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(icr1.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   AND latest.max_version = icr1.version
            ) icr
                    ON CONVERT(icr.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            WHERE (
                UPPER(ic.status) LIKE '%CANCEL%'
                OR cd.call_number IS NOT NULL
                OR wt.workflow_transition_id IS NOT NULL
            )
            AND (
                icr.call_number IS NULL
                OR UPPER(icr.status) = 'FAILED'
            )
            GROUP BY
                ph.case_no,
                ic.created_at,
                ic.updated_at,
                ic.plant_id,
                rpp.poi_code,
                pm.ibs_vendor_code,
                um.employee_code,
                cd.created_by,
                wt.created_by,
                ic.created_by,
                ic.call_type,
                ic.po_no,
                ic.po_sr,
                cd.created_date,
                wt.created_date,
                ic.total_qty,
                ic.call_no,
                cd.cancellation_basis,
                cd.final_cancellation_charges,
                cd.calculated_charges,
                vfl_c.liability_type,
                vfl_c.amount
            """, nativeQuery = true)
    List<Object[]> getRailpadCancelledInspectionCalls();
}
