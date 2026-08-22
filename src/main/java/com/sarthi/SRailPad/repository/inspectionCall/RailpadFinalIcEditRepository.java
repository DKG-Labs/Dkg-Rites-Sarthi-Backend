package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailpadFinalIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailpadFinalIcEditRepository extends JpaRepository<RailpadFinalIcEdit, Long> {
    Optional<RailpadFinalIcEdit> findByIcNumber(String icNumber);

    @Query(value = """
            SELECT
                ph.case_no                                              AS caseNumber,
                DATE(ic.created_at)                                     AS callDate,
                ic.plant_id                                             AS placeOfInspection,
                COALESCE(pm.ibs_vendor_code, ic.plant_id)               AS ibsManufacturedCode,
                CAST(COALESCE(um.employee_code, f.created_by) AS CHAR)  AS ieEmployeeNumber,
                'A'                                                     AS callStatus,
                'F'                                                     AS typeOfCall,
                (CASE 
                    WHEN ic.po_no LIKE '%/%' AND SUBSTRING_INDEX(ic.po_no, '/', -1) <> '' THEN SUBSTRING_INDEX(ic.po_no, '/', -1)
                    WHEN ic.po_sr IS NOT NULL AND TRIM(ic.po_sr) <> '' THEN TRIM(ic.po_sr)
                    ELSE '1'
                END)                                                    AS poItemSerialNumber,
                CAST(f.book_no AS CHAR)                                 AS bkNumber,
                CAST(f.set_no AS CHAR)                                  AS setNumber,
                DATE(f.created_at)                                      AS icDate,
                COALESCE(SUM(res.offered_qty), ic.total_qty, 0)         AS quantityOffered,
                COALESCE(SUM(res.accepted_qty), 0)                      AS quantityPassed,
                COALESCE(SUM(res.rejected_qty), 0)                      AS quantityRejected,
                ic.call_no                                              AS callNo,
                f.ic_number                                             AS callNumber
            FROM railpad_final_ic_edit f
            INNER JOIN rail_inspection_call ic
                    ON ic.call_no COLLATE utf8mb4_unicode_ci = 
                       SUBSTRING_INDEX(SUBSTRING_INDEX(f.ic_number, '/', 2), '/', -1) COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_header ph
                   ON ph.po_no COLLATE utf8mb4_unicode_ci = 
                      (CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um
                   ON CAST(um.userid AS CHAR) = CAST(f.created_by AS CHAR)
                   OR um.employee_code = f.created_by
            LEFT JOIN rail_final_inspection_lot_results res
                   ON res.call_no COLLATE utf8mb4_unicode_ci = ic.call_no COLLATE utf8mb4_unicode_ci
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
                f.created_by,
                ic.po_no,
                ic.po_sr,
                f.book_no,
                f.set_no,
                f.created_at,
                ic.call_no,
                f.ic_number,
                ic.total_qty
            """, nativeQuery = true)
    List<Object[]> getRailpadFinalInspectionCalls();
}

