package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailpadProcessIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailpadProcessIcEditRepository extends JpaRepository<RailpadProcessIcEdit, Long> {
    Optional<RailpadProcessIcEdit> findByIcNumber(String icNumber);

    @Query(value = """
            SELECT
                ph.case_no                                              AS caseNumber,
                DATE(ic.created_at)                                     AS callDate,
                ic.plant_id                                             AS placeOfInspection,
                COALESCE(pm.ibs_vendor_code, ic.plant_id)               AS ibsManufacturedCode,
                CAST(COALESCE(um.employee_code, p.created_by) AS CHAR)  AS ieEmployeeNumber,
                'A'                                                     AS callStatus,
                'P'                                                     AS typeOfCall,
                (CASE 
                    WHEN ic.po_no LIKE '%/%' AND SUBSTRING_INDEX(ic.po_no, '/', -1) <> '' THEN SUBSTRING_INDEX(ic.po_no, '/', -1)
                    WHEN ic.po_sr IS NOT NULL AND TRIM(ic.po_sr) <> '' THEN TRIM(ic.po_sr)
                    ELSE '1'
                END)                                                    AS poItemSerialNumber,
                CAST(p.book_no AS CHAR)                                 AS bkNumber,
                CAST(p.set_no AS CHAR)                                  AS setNumber,
                DATE(p.created_at)                                      AS icDate,
                COALESCE(CAST(NULLIF(p.qty_now_offered, '') AS UNSIGNED), ic.total_qty, 0) AS quantityOffered,
                COALESCE(CAST(NULLIF(p.qty_now_passed, '') AS UNSIGNED), 0)  AS quantityPassed,
                COALESCE(CAST(NULLIF(p.qty_now_rejected, '') AS UNSIGNED), 0) AS quantityRejected,
                ic.call_no                                              AS callNo,
                p.ic_number                                             AS callNumber
            FROM railpad_process_ic_edit p
            INNER JOIN rail_inspection_call ic
                    ON ic.call_no COLLATE utf8mb4_unicode_ci = 
                       SUBSTRING_INDEX(SUBSTRING_INDEX(p.ic_number, '/', 2), '/', -1) COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_header ph
                   ON ph.po_no COLLATE utf8mb4_unicode_ci = 
                      (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um
                   ON CAST(um.userid AS CHAR) = CAST(p.created_by AS CHAR)
                   OR um.employee_code = p.created_by
            LEFT JOIN (
                SELECT icr1.*
                FROM ibs_call_registration icr1
                INNER JOIN (
                    SELECT call_number, MAX(version) AS max_version
                    FROM ibs_call_registration
                    GROUP BY call_number
                ) latest
                    ON latest.call_number = icr1.call_number
                   AND latest.max_version = icr1.version
            ) icr
                    ON icr.call_number COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
            LEFT JOIN sarthi_ibs_poi_mapping pm
                   ON pm.poi_code = ic.plant_id
            WHERE icr.call_number IS NULL
               OR UPPER(icr.status) = 'FAILED'
            GROUP BY
                ph.case_no,
                ic.created_at,
                ic.plant_id,
                pm.ibs_vendor_code,
                um.employee_code,
                p.created_by,
                ic.po_no,
                ic.po_sr,
                p.book_no,
                p.set_no,
                p.created_at,
                ic.call_no,
                p.ic_number,
                ic.total_qty,
                p.qty_now_offered,
                p.qty_now_passed,
                p.qty_now_rejected
            """, nativeQuery = true)
    List<Object[]> getRailpadProcessInspectionCalls();
}

