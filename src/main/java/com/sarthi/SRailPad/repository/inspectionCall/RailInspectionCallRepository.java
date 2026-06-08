package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailInspectionCallRepository extends JpaRepository<RailInspectionCall, Long> {
    
    @Query(value = "SELECT call_no FROM rail_inspection_call WHERE call_no LIKE ?1 ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastCallNoByPattern(String pattern);

    @Query("SELECT SUM(c.totalQty) FROM RailInspectionCall c WHERE c.poNo = :poNo")
    Integer findTotalQtyByPoNo(String poNo);

    List<RailInspectionCall> findAllByVendorCode(String vendorCode);

    Optional<RailInspectionCall> findByCallNo(String callNo);

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
}
