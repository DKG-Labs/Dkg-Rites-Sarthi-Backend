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
                COALESCE(ph.case_no, '')                                AS caseNumber,
                DATE(ic.created_at)                                     AS callDate,
                COALESCE(CONVERT(rpp.poi_code USING utf8mb4), CONVERT(ic.plant_id USING utf8mb4)) AS placeOfInspection,
                COALESCE(CONVERT(pm.ibs_vendor_code USING utf8mb4), CONVERT(ic.plant_id USING utf8mb4)) AS ibsManufacturedCode,
                CAST(COALESCE(um_assigned.employee_code, um.employee_code, f.created_by, ic.created_by) AS CHAR) AS ieEmployeeNumber,
                'A'                                                     AS callStatus,
                'F'                                                     AS typeOfCall,
                (CASE 
                    WHEN ic.po_no LIKE '%/%' AND SUBSTRING_INDEX(ic.po_no, '/', -1) <> '' THEN SUBSTRING_INDEX(ic.po_no, '/', -1)
                    WHEN ic.po_sr IS NOT NULL AND TRIM(ic.po_sr) <> '' THEN TRIM(ic.po_sr)
                    ELSE '1'
                END)                                                    AS poItemSerialNumber,
                CAST(COALESCE(f.book_no, '') AS CHAR)                   AS bkNumber,
                CAST(COALESCE(f.set_no, '') AS CHAR)                    AS setNumber,
                DATE(COALESCE(f.created_at, ic.updated_at, ic.created_at)) AS icDate,
                COALESCE(SUM(res.offered_qty), ic.total_qty, 0)         AS quantityOffered,
                COALESCE(SUM(res.accepted_qty), 0)                      AS quantityPassed,
                COALESCE(SUM(res.rejected_qty), 0)                      AS quantityRejected,
                ic.call_no                                              AS callNo,
                COALESCE(
                    NULLIF(ricd.certificate_no, ''),
                    (CASE WHEN f.ic_number LIKE '%/%' THEN f.ic_number ELSE NULL END),
                    CONCAT(
                        COALESCE(
                            NULLIF(LEFT(TRIM(wt_assigned.rio), 1), ''),
                            'C'
                        ),
                        '/',
                        ic.call_no,
                        '/',
                        UPPER(COALESCE(NULLIF(um_assigned.short_name, ''), NULLIF(um.short_name, ''), 'IE'))
                    )
                )                                                       AS callNumber
            FROM railpad_final_ic_edit f
            INNER JOIN rail_inspection_call ic
                    ON CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(f.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                    OR CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                       CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(f.ic_number, '/', 2), '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                    OR CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                       CONVERT(SUBSTRING_INDEX(f.ic_number, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT rwt1.request_id, rwt1.assigned_to_user, rwt1.rio
                FROM rail_workflow_transaction rwt1
                INNER JOIN (
                    SELECT request_id, MAX(workflow_transition_id) AS max_wt_id
                    FROM rail_workflow_transaction
                    WHERE assigned_to_user IS NOT NULL
                    GROUP BY request_id
                ) latest_wt
                    ON rwt1.workflow_transition_id = latest_wt.max_wt_id
            ) wt_assigned
                    ON CONVERT(wt_assigned.request_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um_assigned
                   ON CONVERT(um_assigned.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(wt_assigned.assigned_to_user USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT ricd1.call_no, ricd1.certificate_no
                FROM rail_inspection_complete_details ricd1
                INNER JOIN (
                    SELECT call_no, MAX(id) AS max_id
                    FROM rail_inspection_complete_details
                    GROUP BY call_no
                ) latest_ricd
                    ON ricd1.id = latest_ricd.max_id
            ) ricd
                    ON CONVERT(ricd.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_header ph
                   ON CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT((CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um
                   ON CONVERT(um.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(f.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(f.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN rail_final_inspection_lot_results res
                   ON CONVERT(res.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
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
            LEFT JOIN railpad_pincode_poi_mapping rpp
                   ON CONVERT(REPLACE(TRIM(rpp.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT(SUBSTRING_INDEX(TRIM(ic.plant_id), '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(rpp.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT(REPLACE(TRIM(COALESCE(ic.vendor_code, '')), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN sarthi_ibs_poi_mapping pm
                   ON CONVERT(pm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(rpp.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci
                  AND pm.product_type = 'railpad'
            WHERE icr.call_number IS NULL
               OR UPPER(icr.status) = 'FAILED'
            GROUP BY
                ph.case_no,
                ic.created_at,
                ic.updated_at,
                ic.plant_id,
                rpp.poi_code,
                pm.ibs_vendor_code,
                um_assigned.employee_code,
                um_assigned.short_name,
                um.employee_code,
                um.short_name,
                f.created_by,
                ic.created_by,
                ic.po_no,
                ic.po_sr,
                f.book_no,
                f.set_no,
                f.created_at,
                ic.call_no,
                f.ic_number,
                ricd.certificate_no,
                wt_assigned.rio,
                ic.total_qty
            """, nativeQuery = true)
    List<Object[]> getRailpadFinalInspectionCalls();
}


