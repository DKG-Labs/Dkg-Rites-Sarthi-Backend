package com.sarthi.SRailPad.diversion.repository;

import com.sarthi.SRailPad.entity.inspectionCall.RailInspectionCall;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Read-only lookups of Rail Pad calls whose IC can be a diversion source. */
@org.springframework.stereotype.Repository
public interface RailDiversionSourceQueryRepository extends Repository<RailInspectionCall, Long> {

    /**
     * Issued ICs of a vendor (optionally one plant) for a call type (PROCESS / FINAL).
     * "Issued" = IC generated / e-signed, or already sent to IBS. Diverted-passed calls are excluded
     * (no chained diversions). Row: [call_no, po_no, po_sr, plant_id, certificate_no, ic_date, ie_user_id, rail_pad_type].
     */
    @Query(value = """
            SELECT c.call_no, c.po_no, c.po_sr, c.plant_id, d.CERTIFICATE_NO,
                   (SELECT MIN(t.created_date) FROM rail_workflow_transaction t
                     WHERE t.request_id COLLATE utf8mb4_unicode_ci = c.call_no COLLATE utf8mb4_unicode_ci AND UPPER(t.action) IN ('GENERATE_IC','IC_GENERATION','DSC_SIGN_IC')) AS ic_date,
                   (SELECT t2.assigned_to_user FROM rail_workflow_transaction t2
                     WHERE t2.request_id COLLATE utf8mb4_unicode_ci = c.call_no COLLATE utf8mb4_unicode_ci ORDER BY t2.workflow_transition_id DESC LIMIT 1) AS ie_user_id,
                   c.rail_pad_type
            FROM rail_inspection_call c
            JOIN RAIL_INSPECTION_COMPLETE_DETAILS d ON d.CALL_NO COLLATE utf8mb4_unicode_ci = c.call_no COLLATE utf8mb4_unicode_ci
            WHERE REPLACE(c.vendor_code, ':', '') = REPLACE(:vendorCode, ':', '')
              AND (:plantId IS NULL OR :plantId = '' OR REPLACE(c.plant_id, ':', '') = REPLACE(:plantId, ':', ''))
              AND UPPER(c.call_type) = UPPER(:callType)
              AND COALESCE(c.call_category, 'FRESH') <> 'DIVERTED_PASSED'
              AND (UPPER(c.status) = 'SEND_CALL_TO_IBS'
                   OR EXISTS (SELECT 1 FROM rail_workflow_transaction t3
                              WHERE t3.request_id COLLATE utf8mb4_unicode_ci = c.call_no COLLATE utf8mb4_unicode_ci AND UPPER(t3.action) IN ('GENERATE_IC','IC_GENERATION','DSC_SIGN_IC')))
            ORDER BY c.created_at DESC
            """, nativeQuery = true)
    List<Object[]> findIssuedIcs(@Param("vendorCode") String vendorCode,
                                 @Param("plantId") String plantId,
                                 @Param("callType") String callType);
}
