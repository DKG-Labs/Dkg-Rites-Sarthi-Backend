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

    List<RailInspectionCall> findAllByVendorCode(String vendorCode);

    List<RailInspectionCall> findAllByPlantId(String plantId);

    Page<RailInspectionCall> findByVendorCodeOrderByCreatedAtDesc(String vendorCode, Pageable pageable);

    Page<RailInspectionCall> findByPlantIdOrderByCreatedAtDesc(String plantId, Pageable pageable);

    @Query(value = "SELECT c.* FROM rail_inspection_call c WHERE REPLACE(c.plant_id, ':', '') = REPLACE(:plantId, ':', '') AND (SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1) NOT IN (:statuses) ORDER BY c.created_at DESC",
           countQuery = "SELECT count(c.id) FROM rail_inspection_call c WHERE REPLACE(c.plant_id, ':', '') = REPLACE(:plantId, ':', '') AND (SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1) NOT IN (:statuses)",
           nativeQuery = true)
    Page<RailInspectionCall> findPendingCallsForPlantNative(@Param("plantId") String plantId, @Param("statuses") List<String> statuses, Pageable pageable);
    
    @Query(value = "SELECT c.* FROM rail_inspection_call c WHERE REPLACE(c.plant_id, ':', '') = REPLACE(:plantId, ':', '') AND (SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1) IN (:statuses) ORDER BY c.created_at DESC",
           countQuery = "SELECT count(c.id) FROM rail_inspection_call c WHERE REPLACE(c.plant_id, ':', '') = REPLACE(:plantId, ':', '') AND (SELECT w.status FROM rail_workflow_transaction w WHERE w.request_id = c.call_no ORDER BY w.workflow_transition_id DESC LIMIT 1) IN (:statuses)",
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


}
