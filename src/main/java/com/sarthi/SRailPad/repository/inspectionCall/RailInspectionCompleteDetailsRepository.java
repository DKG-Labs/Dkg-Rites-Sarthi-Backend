package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCompleteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailInspectionCompleteDetailsRepository extends JpaRepository<RailInspectionCompleteDetails, Long> {
    Optional<RailInspectionCompleteDetails> findFirstByCallNoOrderByCreatedOnDesc(String callNo);

    @Query(value = """
        SELECT * FROM RAIL_INSPECTION_COMPLETE_DETAILS d 
        WHERE d.CALL_NO LIKE 'RPP%'
          AND (
            :poSr IS NULL OR :poSr = ''
            OR d.PO_NO LIKE CONCAT('%/', :poSr)
            OR d.PO_NO LIKE CONCAT('%/', LPAD(:poSr, 3, '0'))
            OR (CASE WHEN d.PO_NO LIKE '%/%' THEN TRIM(LEADING '0' FROM SUBSTRING_INDEX(d.PO_NO, '/', -1)) ELSE '' END) = TRIM(LEADING '0' FROM :poSr)
          )
          AND (
            :poNo IS NULL OR :poNo = ''
            OR (CASE WHEN d.PO_NO LIKE '%/%' THEN SUBSTRING_INDEX(d.PO_NO, '/', 1) ELSE d.PO_NO END) = :poNo
          )
        ORDER BY d.CREATED_ON DESC
    """, nativeQuery = true)
    List<RailInspectionCompleteDetails> findProcessCallsByPoNoAndSr(@Param("poNo") String poNo, @Param("poSr") String poSr);
}
