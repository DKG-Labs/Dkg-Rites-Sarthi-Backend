package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperFinalIcEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperFinalIcEditRepository extends JpaRepository<SleeperFinalIcEdit, Long> {
    Optional<SleeperFinalIcEdit> findByIcNumber(String icNumber);

    @Query(value = """
            SELECT
                COALESCE(ph.case_no, '')                                AS caseNumber,
                DATE(sic.created_at)                                    AS callDate,
                COALESCE(CONVERT(pm.poi_code USING utf8mb4), CONVERT(sic.plant_id USING utf8mb4), CONVERT(sppm.poi_code USING utf8mb4)) AS placeOfInspection,
                COALESCE(CONVERT(pm.ibs_vendor_code USING utf8mb4), CONVERT(sic.plant_id USING utf8mb4)) AS ibsManufacturedCode,
                CAST(COALESCE(um_assigned.employee_code, um.employee_code, f.created_by, sic.created_by) AS CHAR) AS ieEmployeeNumber,
                'A'                                                     AS callStatus,
                'F'                                                     AS typeOfCall,
                (CASE 
                    WHEN sic.po_no LIKE '%/%' AND SUBSTRING_INDEX(sic.po_no, '/', -1) <> '' THEN SUBSTRING_INDEX(sic.po_no, '/', -1)
                    WHEN sic.sr_no IS NOT NULL AND TRIM(sic.sr_no) <> '' THEN TRIM(sic.sr_no)
                    ELSE '1'
                END)                                                    AS poItemSerialNumber,
                CAST(COALESCE(f.book_no, '') AS CHAR)                   AS bkNumber,
                CAST(COALESCE(f.set_no, '') AS CHAR)                    AS setNumber,
                DATE(COALESCE(f.created_at, sic.created_at))             AS icDate,
                COALESCE(CAST(sfr.total_offered_quantity AS SIGNED), sic.total_offered, 0) AS quantityOffered,
                COALESCE(CAST(sfr.total_accepted AS SIGNED), 0)         AS quantityPassed,
                COALESCE(CAST(sfr.total_rejected AS SIGNED), 0)         AS quantityRejected,
                sic.call_no                                             AS callNo,
                COALESCE(
                    NULLIF(sicd.certificate_no, ''),
                    (CASE WHEN f.ic_number LIKE '%/%' THEN f.ic_number ELSE NULL END),
                    CONCAT(
                        COALESCE(
                            NULLIF(LEFT(TRIM(wt_assigned.rio), 1), ''),
                            'C'
                        ),
                        '/',
                        sic.call_no,
                        '/',
                        UPPER(COALESCE(NULLIF(um_assigned.short_name, ''), NULLIF(um.short_name, ''), 'IE'))
                    )
                )                                                       AS callNumber
            FROM sleeper_final_ic_edit f
            INNER JOIN sleeper_inspection_call sic
                    ON CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(f.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                    OR CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                       CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(f.ic_number, '/', 2), '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                    OR CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                       CONVERT(SUBSTRING_INDEX(f.ic_number, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT swt1.request_id, swt1.assigned_to_user, swt1.rio
                FROM sleeper_workflow_transaction swt1
                INNER JOIN (
                    SELECT request_id, MAX(workflow_transition_id) AS max_wt_id
                    FROM sleeper_workflow_transaction
                    WHERE assigned_to_user IS NOT NULL
                    GROUP BY request_id
                ) latest_wt
                    ON swt1.workflow_transition_id = latest_wt.max_wt_id
            ) wt_assigned
                    ON CONVERT(wt_assigned.request_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um_assigned
               ON CONVERT(um_assigned.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(wt_assigned.assigned_to_user USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT sicd1.call_no, sicd1.certificate_no
                FROM sleeper_inspection_complete_details sicd1
                INNER JOIN (
                    SELECT call_no, MAX(id) AS max_id
                    FROM sleeper_inspection_complete_details
                    GROUP BY call_no
                ) latest_sicd
                    ON sicd1.id = latest_sicd.max_id
            ) sicd
                    ON CONVERT(sicd.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_header ph
                   ON CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT((CASE WHEN sic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(sic.po_no, '/', 1) ELSE sic.po_no END) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um
                   ON CONVERT(um.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(f.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(f.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT sfr1.call_number, sfr1.total_offered_quantity, sfr1.total_accepted, sfr1.total_rejected
                FROM sleeper_final_result sfr1
                INNER JOIN (
                    SELECT call_number, MAX(id) AS max_id
                    FROM sleeper_final_result
                    GROUP BY call_number
                ) latest_sfr
                    ON sfr1.id = latest_sfr.max_id
            ) sfr
                   ON CONVERT(sfr.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
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
                    ON CONVERT(icr.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN sleeper_pincode_poi_mapping sppm
                   ON CONVERT(REPLACE(TRIM(sppm.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT(SUBSTRING_INDEX(TRIM(sic.plant_id), '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(sppm.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT(CAST(sic.created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN sarthi_ibs_poi_mapping pm
                   ON (
                       CONVERT(pm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci
                       OR CONVERT(pm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
                       OR CONVERT(REPLACE(TRIM(pm.poi_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(sic.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   )
                  AND pm.product_type = 'sleeper'
            WHERE icr.call_number IS NULL
               OR UPPER(icr.status) = 'FAILED'
            GROUP BY
                ph.case_no,
                sic.created_at,
                sic.plant_id,
                sppm.poi_code,
                pm.poi_code,
                pm.ibs_vendor_code,
                um_assigned.employee_code,
                um_assigned.short_name,
                um.employee_code,
                um.short_name,
                f.created_by,
                sic.created_by,
                sic.po_no,
                sic.sr_no,
                f.book_no,
                f.set_no,
                f.created_at,
                sic.call_no,
                f.ic_number,
                sicd.certificate_no,
                wt_assigned.rio,
                sic.total_offered,
                sfr.total_offered_quantity,
                sfr.total_accepted,
                sfr.total_rejected
            """, nativeQuery = true)
    List<Object[]> getSleeperFinalInspectionCalls();
}
