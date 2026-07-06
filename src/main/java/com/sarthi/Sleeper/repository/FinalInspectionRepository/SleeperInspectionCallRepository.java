package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.dto.SleeperDashboardDtos.Level1Projection;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.MprProjection;
import com.sarthi.Sleeper.dto.SleeperDashboardDtos.SleeperIcProjection;
import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperInspectionCallRepository extends JpaRepository<SleeperInspectionCall, Long> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<SleeperInspectionCall> findByCreatedBy(Long createdBy);

    Optional<SleeperInspectionCall> findByCallNo(String callNo);

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.goodSleepers s
""")
    List<Long> findAllGoodSleeperIds();

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.badSleepers s
""")
    List<Long> findAllBadSleeperIds();

    @Query("""
    SELECT c 
    FROM SleeperInspectionCall c
    WHERE c.poNo = :poNo
      AND c.srNo = :srNo
""")
    List<SleeperInspectionCall> getCalls(String poNo, String srNo);

    @Query(value = """

SELECT 
    ph.rly_short_name AS rly,
    ph.po_no AS poNo,
    ph.po_date AS poDate,
    ph.firm_details AS vendor,
    ph.region_code AS region,

    -- 🔹 PO QTY
    (SELECT COALESCE(SUM(pi.qty),0)
     FROM po_item pi
     WHERE pi.po_header_id = ph.id) AS poQty,

    -- 🔹 ACCEPTED
    (SELECT COALESCE(SUM(ibs.total_accepted),0)
     FROM ie_batch_summary ibs
     JOIN sleeper_inspection_call sic
          ON ibs.call_no COLLATE utf8mb4_unicode_ci
           = sic.call_no COLLATE utf8mb4_unicode_ci
     WHERE sic.po_no COLLATE utf8mb4_unicode_ci
           = ph.po_no COLLATE utf8mb4_unicode_ci) AS accQty,

    -- 🔹 REJECTED
    (SELECT COALESCE(SUM(ibs.total_rejected),0)
     FROM ie_batch_summary ibs
     JOIN sleeper_inspection_call sic
          ON ibs.call_no COLLATE utf8mb4_unicode_ci
           = sic.call_no COLLATE utf8mb4_unicode_ci
     WHERE sic.po_no COLLATE utf8mb4_unicode_ci
           = ph.po_no COLLATE utf8mb4_unicode_ci) AS totalRejected,

    -- 🔹 OFFERED
    (SELECT COALESCE(SUM(ibs.total_offered),0)
     FROM ie_batch_summary ibs
     JOIN sleeper_inspection_call sic
          ON ibs.call_no COLLATE utf8mb4_unicode_ci
           = sic.call_no COLLATE utf8mb4_unicode_ci
     WHERE sic.po_no COLLATE utf8mb4_unicode_ci
           = ph.po_no COLLATE utf8mb4_unicode_ci) AS totalOffered,

    -- 🔹 UOM
    (SELECT pi.uom
     FROM po_item pi
     WHERE pi.po_header_id = ph.id
     LIMIT 1) AS uom

FROM po_header ph

WHERE ph.item_cat_descr = 'PSC Mainline Sleeper'
  AND DATE(ph.po_date) BETWEEN :startDate AND :endDate

ORDER BY ph.po_date DESC

""", nativeQuery = true)
    List<Level1Projection> getLevel1Data(
            LocalDate startDate,
            LocalDate endDate
    );
  /*  @Query(value = """

SELECT
    ph.rly_cd AS rly,
    ph.po_no AS poNo,
    ph.vendor_details AS manufacturer,

    -- 🔹 PO QTY
    (SELECT COALESCE(SUM(pi.qty),0)
     FROM po_item pi
     WHERE pi.po_header_id = ph.id) AS poQty,

    -- 🔹 DISPATCHED IN DATE RANGE
    (SELECT COALESCE(SUM(fci.accepted_qty),0)
     FROM final_call_inspection_header fci
     WHERE fci.rly_po_no = ph.po_no
       AND fci.call_date BETWEEN :startDate AND :endDate
    ) AS dispatchedInPeriod,

    -- 🔹 TOTAL DISPATCHED
    (SELECT COALESCE(SUM(fci.accepted_qty),0)
     FROM final_call_inspection_header fci
     WHERE fci.rly_po_no = ph.po_no
    ) AS totalDispatched

FROM po_header ph
WHERE ph.item_cat_descr = 'PSC Mainline Sleeper'

""", nativeQuery = true)
    List<MprProjection> getMprData(LocalDate startDate, LocalDate endDate);  */
  @Query(value = """

SELECT 
    ph.rly_cd AS rly,
    ph.po_no AS poNo,
    ph.vendor_details AS manufacturer,

    -- PO QTY
    (
        SELECT COALESCE(SUM(pi.qty),0)
        FROM po_item pi
        WHERE pi.po_header_id = ph.id
    ) AS poQty,

    -- DISPATCHED IN DATE RANGE
    (
        SELECT COALESCE(SUM(fci.accepted_qty),0)
        FROM final_call_inspection_header fci
        WHERE fci.rly_po_no COLLATE utf8mb4_unicode_ci =
              ph.po_no COLLATE utf8mb4_unicode_ci
        AND fci.call_date BETWEEN :startDate AND :endDate
    ) AS dispatchedInPeriod,

    -- TOTAL DISPATCHED
    (
        SELECT COALESCE(SUM(fci.accepted_qty),0)
        FROM final_call_inspection_header fci
        WHERE fci.rly_po_no COLLATE utf8mb4_unicode_ci =
              ph.po_no COLLATE utf8mb4_unicode_ci
    ) AS totalDispatched

FROM po_header ph
WHERE ph.item_cat_descr = 'PSC Mainline Sleeper'

""", nativeQuery = true)
  List<MprProjection> getMprData(
          @Param("startDate") LocalDate startDate,
          @Param("endDate") LocalDate endDate);

    @Query(value = """

SELECT

    sic.call_no AS callNumber,

    CONCAT('Sleeper - Final')
        AS productAndStageOfInspection,

    CONCAT(sic.po_no, '-', sic.sr_no)
        AS poNumber,

    pi.delivery_date AS deliveryDate,

    pi.extended_delivery_date AS expectedDeliveryDate,

    ph.vendor_details AS vendorName,

    NULL AS inspectionDesiredDate,

    sic.created_at AS callDate,

    (
        SELECT CONCAT(
            um.employee_code,
            ' - ',
            um.full_name
        )

        FROM sleeper_poi_ie_mapping spim

        JOIN user_master um
            ON um.userid = spim.ie_user_id

        WHERE spim.plant_id = sic.plant_id
          AND spim.ie_type = 'Main IE'

        LIMIT 1

    ) AS ieName,

    (
        SELECT CONCAT(
            upcm.cm_employee_code,
            ' - ',
            cmum.full_name
        )

        FROM user_product_cm_mapping upcm

        JOIN user_master cmum
            ON cmum.employee_code = upcm.cm_employee_code

        WHERE upcm.user_employee_code =

        (
            SELECT um.employee_code

            FROM sleeper_poi_ie_mapping spim

            JOIN user_master um
                ON um.userid = spim.ie_user_id

            WHERE spim.plant_id = sic.plant_id
              AND spim.ie_type = 'Main IE'

            LIMIT 1
        )

        AND upcm.product_type = 'SLEEPER'

        LIMIT 1

    ) AS cmName,

    (
        SELECT ifm.rio

        FROM sleeper_pincode_poi_mapping sppm

        JOIN ie_fields_mapping ifm
            ON ifm.pin_code = sppm.pin_code
           AND ifm.product = 'Sleeper'

        WHERE CAST(sppm.vendor_code AS UNSIGNED) =
              sic.created_by

        LIMIT 1

    ) AS ritesRio,

    (
        SELECT

            CASE

                WHEN swt.action = 'CREATED'
                    THEN 'Pending for Call Desk Verification'

                WHEN swt.action = 'VERIFY'
                    THEN 'Pending - Assigned to IE'

                WHEN swt.action = 'MAIN_IE_SCHEDULE_CALL'
                    THEN 'Pending - Schedule'

                WHEN swt.action IN (
                    'INITIATE_CALL',
                    'PO_VERIFICATION',
                    'PAUSE'
                )
                    THEN 'Under Inspection'

                WHEN swt.action = 'FINISH'
                    THEN 'Completed (Pending for IC Issue)'

                ELSE 'Under Inspection'

            END

        FROM sleeper_workflow_transaction swt

        WHERE swt.workflow_transition_id = (

            SELECT MAX(swt2.workflow_transition_id)

            FROM sleeper_workflow_transaction swt2

            WHERE swt2.request_id = sic.call_no

        )

    ) AS status

FROM sleeper_inspection_call sic

LEFT JOIN po_header ph
    ON ph.po_no = sic.po_no

LEFT JOIN po_item pi
    ON pi.po_header_id = ph.id
   AND pi.item_sr_no =
       SUBSTRING_INDEX(sic.sr_no, '/', -1)

WHERE sic.created_at BETWEEN :fromDate AND :toDate

ORDER BY sic.created_at DESC

""", nativeQuery = true)
    List<Object[]> getSleeperInspectionReport(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query(value = """

    SELECT

        sic.call_no AS callNumber,

        CONCAT('Sleeper - Final')
            AS productAndStageOfInspection,

        CONCAT(sic.po_no, '-', sic.sr_no)
            AS poNumber,

        NULL AS deliveryDate,

        NULL AS expectedDeliveryDate,

        ph.vendor_details AS vendorName,

        sic.desired_inspection_date AS inspectionDesiredDate,

        sic.created_at AS callDate,

        (
            SELECT um.username

            FROM sleeper_poi_ie_mapping spim

            JOIN user_master um
                ON um.userid = spim.ie_user_id

            WHERE spim.plant_id = sic.plant_id
              AND spim.ie_type = 'Main IE'

            LIMIT 1
        ) AS ieName,

        (
            SELECT upcm.cm_employee_code

            FROM user_product_cm_mapping upcm

            WHERE upcm.user_employee_code =

            (
                SELECT um.employee_code

                FROM sleeper_poi_ie_mapping spim

                JOIN user_master um
                    ON um.userid = spim.ie_user_id

                WHERE spim.plant_id = sic.plant_id
                  AND spim.ie_type = 'Main IE'

                LIMIT 1
            )

            AND upcm.product_type = 'SLEEPER'

            LIMIT 1

        ) AS cmName,

        (
            SELECT ifm.rio

            FROM sleeper_pincode_poi_mapping sppm

            JOIN ie_fields_mapping ifm
                ON ifm.pin_code = sppm.pin_code
                AND ifm.product = 'Sleeper'

            WHERE sppm.vendor_code = sic.created_by

            LIMIT 1

        ) AS ritesRio,

        (
            SELECT

                CASE

                    WHEN swt.action = 'CREATED'
                        THEN 'Pending for Call Desk Verification'

                    WHEN swt.action = 'VERIFY'
                        THEN 'Pending - Assigned to IE'

                    WHEN swt.action = 'MAIN_IE_SCHEDULE_CALL'
                        THEN 'Pending - Schedule'

                    WHEN swt.action IN (
                        'INITIATE_CALL',
                        'PO_VERIFICATION',
                        'PAUSE'
                    )
                        THEN 'Under Inspection'

                    WHEN swt.action = 'FINISH'
                        THEN 'Completed (Pending for IC Issue)'

                    ELSE 'Under Inspection'

                END

            FROM sleeper_workflow_transaction swt

            WHERE swt.workflow_transition_id = (

                SELECT MAX(swt2.workflow_transition_id)

                FROM sleeper_workflow_transaction swt2

                WHERE swt2.request_id = sic.call_no

            )

        ) AS status

    FROM sleeper_inspection_call sic

    LEFT JOIN po_header ph
        ON ph.po_no = sic.po_no

    WHERE sic.created_at BETWEEN :fromDate AND :toDate

      AND sic.desired_inspection_date < CURDATE()

      AND (

            SELECT swt.job_status

            FROM sleeper_workflow_transaction swt

            WHERE swt.workflow_transition_id = (

                SELECT MAX(swt2.workflow_transition_id)

                FROM sleeper_workflow_transaction swt2

                WHERE swt2.request_id = sic.call_no

            )

        ) = 'RIO_VERIFIED'

    ORDER BY sic.created_at DESC

    """, nativeQuery = true)
    List<Object[]> getSleeperOverduePendingCallsReport(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query(value = """

    SELECT

        um.employee_code AS ieId,

        um.username AS ieName,

        COALESCE((
            SELECT COUNT(*)

            FROM sleeper_inspection_call sic

            WHERE EXISTS (

                SELECT 1

                FROM sleeper_poi_ie_mapping spim

                WHERE spim.plant_id COLLATE utf8mb4_unicode_ci =
                      sic.plant_id COLLATE utf8mb4_unicode_ci

                  AND spim.ie_user_id = um.userid
            )

            AND (

                SELECT swt.action

                FROM sleeper_workflow_transaction swt

                WHERE swt.workflow_transition_id = (

                    SELECT MAX(swt2.workflow_transition_id)

                    FROM sleeper_workflow_transaction swt2

                    WHERE swt2.request_id COLLATE utf8mb4_unicode_ci =
                          sic.call_no COLLATE utf8mb4_unicode_ci
                )

            ) IN (
                'VERIFY'
            )

        ),0) AS noOfCallsPending,

        COALESCE((
            SELECT COUNT(*)

            FROM sleeper_inspection_call sic

            WHERE EXISTS (

                SELECT 1

                FROM sleeper_poi_ie_mapping spim

                WHERE spim.plant_id COLLATE utf8mb4_unicode_ci =
                      sic.plant_id COLLATE utf8mb4_unicode_ci

                  AND spim.ie_user_id = um.userid
            )

            AND (

                SELECT swt.action

                FROM sleeper_workflow_transaction swt

                WHERE swt.workflow_transition_id = (

                    SELECT MAX(swt2.workflow_transition_id)

                    FROM sleeper_workflow_transaction swt2

                    WHERE swt2.request_id COLLATE utf8mb4_unicode_ci =
                          sic.call_no COLLATE utf8mb4_unicode_ci
                )

            ) IN (
                'INITIATE_CALL',
                'PO_VERIFICATION',
                'PAUSE'
            )

        ),0) AS noOfCallsUnderInspection,

        COALESCE((
            SELECT COUNT(*)

            FROM sleeper_inspection_call sic

            WHERE EXISTS (

                SELECT 1

                FROM sleeper_poi_ie_mapping spim

                WHERE spim.plant_id COLLATE utf8mb4_unicode_ci =
                      sic.plant_id COLLATE utf8mb4_unicode_ci

                  AND spim.ie_user_id = um.userid
            )

            AND (

                SELECT swt.action

                FROM sleeper_workflow_transaction swt

                WHERE swt.workflow_transition_id = (

                    SELECT MAX(swt2.workflow_transition_id)

                    FROM sleeper_workflow_transaction swt2

                    WHERE swt2.request_id COLLATE utf8mb4_unicode_ci =
                          sic.call_no COLLATE utf8mb4_unicode_ci
                )

            ) = 'FINISH'

        ),0) AS noOfCallsPendingForIc,

        COALESCE((
            SELECT COUNT(*)

            FROM sleeper_inspection_call sic

            WHERE sic.desired_inspection_date < CURDATE()

              AND EXISTS (

                    SELECT 1

                    FROM sleeper_poi_ie_mapping spim

                    WHERE spim.plant_id COLLATE utf8mb4_unicode_ci =
                          sic.plant_id COLLATE utf8mb4_unicode_ci

                      AND spim.ie_user_id = um.userid
              )

              AND (

                    SELECT swt.job_status

                    FROM sleeper_workflow_transaction swt

                    WHERE swt.workflow_transition_id = (

                        SELECT MAX(swt2.workflow_transition_id)

                        FROM sleeper_workflow_transaction swt2

                        WHERE swt2.request_id COLLATE utf8mb4_unicode_ci =
                              sic.call_no COLLATE utf8mb4_unicode_ci
                    )

              ) = 'RIO_VERIFIED'

        ),0) AS noOfCallsOverdue

    FROM user_master um

    JOIN user_role_master urm
        ON urm.userid = um.userid

    JOIN user_product_cm_mapping upcm
        ON upcm.user_employee_code COLLATE utf8mb4_unicode_ci =
           um.employee_code COLLATE utf8mb4_unicode_ci

        AND upcm.product_type = 'SLEEPER'

    WHERE urm.roleid IN (10)

      AND upcm.cm_employee_code = :cmEmployeeCode

    ORDER BY um.employee_code

    """, nativeQuery = true)
    List<Object[]> getSleeperIeWiseCallStatusWorkloadSummary(
            @Param("cmEmployeeCode") String cmEmployeeCode
    );


    @Query(value = """

SELECT

    um.employee_code AS ieId,

    um.username AS ieName,

    COUNT(DISTINCT fc.call_no) AS totalCalls,

    COUNT(DISTINCT CASE
        WHEN fc.isOverdue = 1
        THEN fc.call_no
    END) AS overdueCallsAttended,

    COUNT(DISTINCT CASE
        WHEN fc.finalStatus = 'ACCEPTED'
        THEN fc.call_no
    END) AS callsAccepted,

    COUNT(DISTINCT CASE
        WHEN fc.finalStatus = 'REJECTED'
        THEN fc.call_no
    END) AS callsRejected,

    COUNT(DISTINCT CASE
        WHEN fc.finalStatus = 'PARTIAL'
        THEN fc.call_no
    END) AS callsPartiallyAcceptedRejected,

    COUNT(DISTINCT CASE
        WHEN fc.icIssued = 1
        THEN fc.call_no
    END) AS icIssued

FROM user_master um

JOIN user_role_master urm
    ON urm.userid = um.userid

JOIN user_product_cm_mapping upcm
    ON upcm.user_employee_code = um.employee_code
    AND upcm.product_type = 'SLEEPER'

LEFT JOIN (

    SELECT DISTINCT

        sic.call_no,

        sic.plant_id,

        CASE

            -- ACCEPTED
            WHEN fcih.qty_offered_now = fcih.accepted_qty
                 AND fcih.rejected_qty = 0
            THEN 'ACCEPTED'

            -- REJECTED
            WHEN fcih.qty_offered_now = fcih.rejected_qty
            THEN 'REJECTED'

            -- PARTIAL
            WHEN fcih.rejected_qty > 0
                 AND fcih.rejected_qty < fcih.qty_offered_now
            THEN 'PARTIAL'

            ELSE NULL

        END AS finalStatus,

        -- OVERDUE
        CASE

            WHEN sic.desired_inspection_date < (

                SELECT DATE(swt.created_date)

                FROM sleeper_workflow_transaction swt

                WHERE swt.workflow_transition_id = (

                    SELECT MIN(swt2.workflow_transition_id)

                    FROM sleeper_workflow_transaction swt2

                    WHERE swt2.request_id = sic.call_no
                      AND swt2.action = 'MAIN_IE_SCHEDULE_CALL'
                )

            )

            THEN 1
            ELSE 0

        END AS isOverdue,

        -- IC ISSUED
       CASE

            WHEN (

                SELECT swt.status

                FROM sleeper_workflow_transaction swt

                WHERE swt.workflow_transition_id = (

                    SELECT MAX(swt2.workflow_transition_id)

                    FROM sleeper_workflow_transaction swt2

                    WHERE swt2.request_id = sic.call_no
                )

            ) = 'DSC_SIGN_IC'

            THEN 1
            ELSE 0

        END AS icIssued

    FROM sleeper_inspection_call sic

    JOIN final_call_inspection_header fcih
        ON fcih.call_no = sic.call_no

    WHERE (

        SELECT swt.action

        FROM sleeper_workflow_transaction swt

        WHERE swt.workflow_transition_id = (

            SELECT MAX(swt2.workflow_transition_id)

            FROM sleeper_workflow_transaction swt2

            WHERE swt2.request_id = sic.call_no
        )

    ) IN (
        'FINISH',
        'GENERATE_IC',
        'DSC_SIGN_IC'
    )

) fc

ON EXISTS (

    SELECT 1

    FROM sleeper_poi_ie_mapping spim

    WHERE spim.plant_id = fc.plant_id
      AND spim.ie_user_id = um.userid

)

WHERE urm.roleid IN (10)

AND upcm.cm_employee_code = :cmEmployeeCode

GROUP BY
    um.employee_code,
    um.username

ORDER BY um.employee_code

""", nativeQuery = true)
    List<Object[]> getSleeperIeOperationalSlaPerformanceSummary(
            @Param("cmEmployeeCode") String cmEmployeeCode
    );


    @Query(value = """

    SELECT

        NULL AS certificateNo,

        NULL AS date,

        (
            SELECT COUNT(*)
            FROM sleeper_inspection_call sic2
            WHERE sic2.po_no = sic.po_no
        ) AS offeredInstallmentNumber,

        NULL AS passedInstallmentNumber,

        ph.vendor_details AS contractor,

        ph.vendor_details AS placeOfInspection,

        CONCAT(sic.po_no, ' / ', sic.created_at) AS contractRefAndDate,

        ph.bill_pay_off_name AS billPayingOffice,

        pi.consignee_detail AS consignee,

        ph.purchaser_detail AS purchasingAuthority,

        sic.sr_no AS itemNo,

        pi.item_desc AS descriptionOfStores,

        CONCAT(
            pi.uom,
            ' - ',
            pi.qty
        ) AS quantityOnOrder,

        CONCAT(
            pi.uom,
            ' - ',
            COALESCE(
                (
                    SELECT SUM(sic2.total_offered)
                    FROM sleeper_inspection_call sic2
                    WHERE sic2.po_no = sic.po_no
                    AND sic2.sr_no = sic.sr_no
                    AND sic2.created_at < sic.created_at
                ),
                0
            )
        ) AS cumulativeQtyOfferedPreviously,

        CONCAT(
            pi.uom,
            ' - ',
            COALESCE(
                (
                    SELECT SUM(
                        sic2.total_offered - sic2.total_rejected
                    )
                    FROM sleeper_inspection_call sic2
                    WHERE sic2.po_no = sic.po_no
                    AND sic2.sr_no = sic.sr_no
                    AND sic2.created_at < sic.created_at
                ),
                0
            )
        ) AS quantityPreviouslyPassed,

        CONCAT(
            pi.uom,
            ' - ',
            sic.total_offered
        ) AS qtyNowOffered,

        CONCAT(
            pi.uom,
            ' - ',
            COALESCE(fcih.accepted_qty, 0)
        ) AS qtyNowPassed,

        CONCAT(
            pi.uom,
            ' - ',
            COALESCE(fcih.rejected_qty, 0)
        ) AS qtyNowRejected,

        CONCAT(
            pi.uom,
            ' - ',
            (
                COALESCE(pi.qty, 0)

                -

                COALESCE(
                    (
                        SELECT SUM(
                            sic2.total_offered - sic2.total_rejected
                        )
                        FROM sleeper_inspection_call sic2
                        WHERE sic2.po_no = sic.po_no
                        AND sic2.sr_no = sic.sr_no
                        AND sic2.created_at < sic.created_at
                    ),
                    0
                )

                -

                COALESCE(fcih.accepted_qty, 0)
            )
        ) AS qtyStillDue,

        sic.desired_inspection_date AS dateOfCall,

        1 AS noOfVisits,

        fcih.created_date AS dateOfInspection,

        (
            SELECT GROUP_CONCAT(
                DISTINCT ibs2.batch_no
                SEPARATOR ', '
            )
            FROM ie_batch_summary ibs2
            WHERE ibs2.call_no = sic.call_no
        ) AS quantityNowPassedBatchNos

    FROM sleeper_inspection_call sic

    LEFT JOIN po_header ph
        ON ph.po_no = sic.po_no

    LEFT JOIN po_item pi
        ON pi.po_header_id = ph.id
        AND pi.item_sr_no = sic.sr_no

    LEFT JOIN final_call_inspection_header fcih
        ON fcih.call_no = sic.call_no

    WHERE sic.call_no = :callNo
    LIMIT 1

    """, nativeQuery = true)
    SleeperIcProjection getSleeperIcData(
            @Param("callNo") String callNo);

}
