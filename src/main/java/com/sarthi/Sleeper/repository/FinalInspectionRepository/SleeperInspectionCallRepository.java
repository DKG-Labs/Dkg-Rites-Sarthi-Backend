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

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"batchesSelected"})
    List<SleeperInspectionCall> findByCreatedBy(Long createdBy);

    Optional<SleeperInspectionCall> findByCallNo(String callNo);
    List<SleeperInspectionCall> findByCallNoIn(Collection<String> callNos);
    boolean existsByCallNo(String callNo);
    List<SleeperInspectionCall> findByPoNoOrderByIdAsc(String poNo);

    @Query("SELECT DISTINCT sic.callNo FROM SleeperInspectionCall sic WHERE (UPPER(COALESCE(sic.status, '')) LIKE '%SEND_CALL_TO_IBS%' OR UPPER(COALESCE(sic.status, '')) LIKE '%SENT_TO_IBS%') AND UPPER(COALESCE(sic.status, '')) NOT LIKE '%CANCEL%'")
    List<String> findPendingIbsCallNumbers();

    @Query("""
        SELECT DISTINCT swt.requestId 
        FROM SleeperWorkflowTransaction swt 
        WHERE (UPPER(COALESCE(swt.status, '')) IN ('SEND_CALL_TO_IBS', 'SENT_TO_IBS') 
               OR UPPER(COALESCE(swt.action, '')) IN ('SEND_CALL_TO_IBS', 'SENT_TO_IBS', 'SEND CALL TO IBS'))
          AND swt.requestId NOT IN (
              SELECT DISTINCT swt2.requestId 
              FROM SleeperWorkflowTransaction swt2 
              WHERE UPPER(COALESCE(swt2.status, '')) LIKE '%CANCEL%' 
                 OR UPPER(COALESCE(swt2.action, '')) LIKE '%CANCEL%'
                 OR UPPER(COALESCE(swt2.jobStatus, '')) LIKE '%CANCEL%'
          )
    """)
    List<String> findPendingWorkflowCallNumbers();

    @Query("""
SELECT DISTINCT c
FROM SleeperInspectionCall c
LEFT JOIN FETCH c.batchesSelected b
WHERE c.callNo = :callNo
""")
    Optional<SleeperInspectionCall> findByCallNoWithBatches(@Param("callNo") String callNo);

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.goodSleepers s
WHERE s.sleeperId IS NOT NULL AND s.sleeperId > 0
  AND b.inspectionCall.status NOT IN ('CANCELLED', 'WITHDRAWN', 'REJECTED')
  AND (:sleeperType IS NULL OR UPPER(TRIM(b.inspectionCall.sleeperType)) = UPPER(TRIM(:sleeperType)))
  AND (:vendorId IS NULL OR b.inspectionCall.createdBy = :vendorId)
""")
    List<Long> findAllGoodSleeperIds(@Param("sleeperType") String sleeperType, @Param("vendorId") Long vendorId);

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.goodSleepers s
WHERE s.sleeperId IS NOT NULL AND s.sleeperId > 0
  AND b.inspectionCall.status NOT IN ('CANCELLED', 'WITHDRAWN', 'REJECTED')
  AND (:sleeperType IS NULL OR UPPER(TRIM(b.inspectionCall.sleeperType)) = UPPER(TRIM(:sleeperType)))
  AND (:vendorId IS NULL OR b.inspectionCall.createdBy = :vendorId)
  AND (:excludeCallNo IS NULL OR UPPER(TRIM(b.inspectionCall.callNo)) <> UPPER(TRIM(:excludeCallNo)))
""")
    List<Long> findAllGoodSleeperIdsExcludingCall(@Param("sleeperType") String sleeperType, @Param("vendorId") Long vendorId, @Param("excludeCallNo") String excludeCallNo);

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.badSleepers s
WHERE s.sleeperId IS NOT NULL AND s.sleeperId > 0
  AND b.inspectionCall.status NOT IN ('CANCELLED', 'WITHDRAWN', 'REJECTED')
  AND (:sleeperType IS NULL OR UPPER(TRIM(b.inspectionCall.sleeperType)) = UPPER(TRIM(:sleeperType)))
  AND (:vendorId IS NULL OR b.inspectionCall.createdBy = :vendorId)
""")
    List<Long> findAllBadSleeperIds(@Param("sleeperType") String sleeperType, @Param("vendorId") Long vendorId);

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.badSleepers s
WHERE s.sleeperId IS NOT NULL AND s.sleeperId > 0
  AND b.inspectionCall.status NOT IN ('CANCELLED', 'WITHDRAWN', 'REJECTED')
  AND (:sleeperType IS NULL OR UPPER(TRIM(b.inspectionCall.sleeperType)) = UPPER(TRIM(:sleeperType)))
  AND (:vendorId IS NULL OR b.inspectionCall.createdBy = :vendorId)
  AND (:excludeCallNo IS NULL OR UPPER(TRIM(b.inspectionCall.callNo)) <> UPPER(TRIM(:excludeCallNo)))
""")
    List<Long> findAllBadSleeperIdsExcludingCall(@Param("sleeperType") String sleeperType, @Param("vendorId") Long vendorId, @Param("excludeCallNo") String excludeCallNo);

    // Same as findAllGood/BadSleeperIds[ExcludingCall](null, null, ...) but restricted to the given
    // sleeper ids, so callers only fetch the ids they will actually look up (completed-batches used to
    // load every raised sleeper id - ~1.25M rows - on each request). Call in chunks of <= 1000 ids.
    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.goodSleepers s
WHERE s.sleeperId IN :ids
  AND s.sleeperId > 0
  AND b.inspectionCall.status NOT IN ('CANCELLED', 'WITHDRAWN', 'REJECTED')
  AND (:excludeCallNo IS NULL OR UPPER(TRIM(b.inspectionCall.callNo)) <> UPPER(TRIM(:excludeCallNo)))
""")
    List<Long> findRaisedGoodSleeperIdsIn(@Param("ids") java.util.Collection<Long> ids, @Param("excludeCallNo") String excludeCallNo);

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.badSleepers s
WHERE s.sleeperId IN :ids
  AND s.sleeperId > 0
  AND b.inspectionCall.status NOT IN ('CANCELLED', 'WITHDRAWN', 'REJECTED')
  AND (:excludeCallNo IS NULL OR UPPER(TRIM(b.inspectionCall.callNo)) <> UPPER(TRIM(:excludeCallNo)))
""")
    List<Long> findRaisedBadSleeperIdsIn(@Param("ids") java.util.Collection<Long> ids, @Param("excludeCallNo") String excludeCallNo);

    @Query("""
SELECT DISTINCT CONCAT(TRIM(b.batchNo), '_', TRIM(s.sleeperNo))
FROM SleeperInspectionCallBatch b
JOIN b.badSleepers s
WHERE s.sleeperNo IS NOT NULL
  AND b.inspectionCall.status NOT IN ('CANCELLED', 'WITHDRAWN', 'REJECTED')
  AND (:sleeperType IS NULL OR UPPER(TRIM(b.inspectionCall.sleeperType)) = UPPER(TRIM(:sleeperType)))
  AND (:vendorId IS NULL OR b.inspectionCall.createdBy = :vendorId)
""")
    List<String> findAllRaisedBadSleeperKeys(@Param("sleeperType") String sleeperType, @Param("vendorId") Long vendorId);

    @Query("""
SELECT DISTINCT CONCAT(TRIM(b.batchNo), '_', TRIM(s.sleeperNo))
FROM SleeperInspectionCallBatch b
JOIN b.badSleepers s
WHERE s.sleeperNo IS NOT NULL
  AND b.inspectionCall.status NOT IN ('CANCELLED', 'WITHDRAWN', 'REJECTED')
  AND (:sleeperType IS NULL OR UPPER(TRIM(b.inspectionCall.sleeperType)) = UPPER(TRIM(:sleeperType)))
  AND (:vendorId IS NULL OR b.inspectionCall.createdBy = :vendorId)
  AND (:excludeCallNo IS NULL OR UPPER(TRIM(b.inspectionCall.callNo)) <> UPPER(TRIM(:excludeCallNo)))
""")
    List<String> findAllRaisedBadSleeperKeysExcludingCall(@Param("sleeperType") String sleeperType, @Param("vendorId") Long vendorId, @Param("excludeCallNo") String excludeCallNo);

    @Query("""
SELECT DISTINCT b.batchNo
FROM SleeperInspectionCallBatch b
WHERE UPPER(TRIM(b.inspectionCall.callNo)) = UPPER(TRIM(:callNo))
""")
    List<String> findBatchNosByCallNo(@Param("callNo") String callNo);

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
    ph.po_no          AS poNo,
    ph.po_date        AS poDate,
    ph.firm_details   AS vendor,
    ph.region_code    AS region,

    COALESCE(pi_agg.poQty, 0)          AS poQty,
    pi_agg.uom                          AS uom,
    COALESCE(ibs_agg.accQty, 0)        AS accQty,
    COALESCE(ibs_agg.totalRejected, 0) AS totalRejected,
    COALESCE(ibs_agg.totalOffered, 0)  AS totalOffered

FROM po_header ph

LEFT JOIN (
    SELECT po_header_id,
           SUM(qty)  AS poQty,
           MIN(uom)  AS uom
    FROM po_item
    GROUP BY po_header_id
) pi_agg ON pi_agg.po_header_id = ph.id

LEFT JOIN (
    SELECT sic.po_no,
           SUM(ibs.total_accepted) AS accQty,
           SUM(ibs.total_rejected) AS totalRejected,
           SUM(ibs.total_offered)  AS totalOffered
    FROM ie_batch_summary ibs
    JOIN sleeper_inspection_call sic
         ON ibs.call_no COLLATE utf8mb4_unicode_ci
          = sic.call_no COLLATE utf8mb4_unicode_ci
    GROUP BY sic.po_no
) ibs_agg
    ON ibs_agg.po_no COLLATE utf8mb4_unicode_ci
     = ph.po_no      COLLATE utf8mb4_unicode_ci

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

  /**
   * PO wise monthly progress, one row per PO and unit of measurement (Nos. / Set).
   * Accepted quantity comes from the final result of the calls whose latest status is SEND_CALL_TO_IBS;
   * the period column counts the ICs issued in the date range, the total counts all ICs of the PO and unit.
   */
  @Query(value = """
SELECT
    COALESCE(NULLIF(TRIM(ph.rly_short_name), ''), ph.rly_cd) AS rly,
    ph.po_no AS poNo,
    ph.vendor_details AS manufacturer,
    CAST(pu.po_qty AS SIGNED) AS poQty,
    CAST(COALESCE(a.period_acc, 0) AS SIGNED) AS dispatchedInPeriod,
    CAST(COALESCE(a.total_acc, 0) AS SIGNED) AS totalDispatched,
    pu.uom AS uom
FROM po_header ph
INNER JOIN (
    SELECT pi.po_header_id AS ph_id,
           CASE WHEN LOWER(TRIM(pi.uom)) LIKE 'set%' THEN 'Set' ELSE 'Nos.' END AS uom,
           COALESCE(SUM(pi.qty), 0) AS po_qty
    FROM po_item pi
    GROUP BY pi.po_header_id,
             CASE WHEN LOWER(TRIM(pi.uom)) LIKE 'set%' THEN 'Set' ELSE 'Nos.' END
) pu ON pu.ph_id = ph.id
LEFT JOIN (
    SELECT c.ph_id AS ph_id,
           c.uom AS uom,
           SUM(c.acc) AS total_acc,
           SUM(CASE WHEN DATE(c.ic_dt) BETWEEN :startDate AND :endDate THEN c.acc ELSE 0 END) AS period_acc
    FROM (
        SELECT ph2.id AS ph_id,
               CASE WHEN LOWER(TRIM(COALESCE(
                        (SELECT pi2.uom FROM po_item pi2
                          WHERE pi2.po_header_id = ph2.id
                            AND (pi2.item_sr_no =CONVERT(sic.sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                              OR pi2.item_sr_no =CONVERT(SUBSTRING_INDEX(sic.sr_no, '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                              OR pi2.item_sr_no =CONVERT(SUBSTRING_INDEX(sic.sr_no, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci)
                          ORDER BY pi2.id LIMIT 1),
                        (SELECT pi3.uom FROM po_item pi3 WHERE pi3.po_header_id = ph2.id ORDER BY pi3.id LIMIT 1),
                        ''))) LIKE 'set%' THEN 'Set' ELSE 'Nos.' END AS uom,
               COALESCE(sfied.ic_dt, sicd.created_on, swt.created_date) AS ic_dt,
               CASE WHEN LOWER(TRIM(COALESCE(
                        (SELECT pi4.uom FROM po_item pi4
                          WHERE pi4.po_header_id = ph2.id
                            AND (pi4.item_sr_no =CONVERT(sic.sr_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                              OR pi4.item_sr_no =CONVERT(SUBSTRING_INDEX(sic.sr_no, '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                              OR pi4.item_sr_no =CONVERT(SUBSTRING_INDEX(sic.sr_no, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci)
                          ORDER BY pi4.id LIMIT 1),
                        (SELECT pi5.uom FROM po_item pi5 WHERE pi5.po_header_id = ph2.id ORDER BY pi5.id LIMIT 1),
                        ''))) LIKE 'set%'
                    THEN COALESCE(sfr.acc_sets, sfr.acc_nos, 0)
                    ELSE COALESCE(sfr.acc_nos, sfr.acc_sets, 0) END AS acc
        FROM sleeper_inspection_call sic
        INNER JOIN (
            SELECT request_id, MAX(workflow_transition_id) AS max_id
            FROM sleeper_workflow_transaction
            WHERE workflow_id = 2
            GROUP BY request_id
        ) latest ON sic.call_no = CONVERT(latest.request_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
        INNER JOIN sleeper_workflow_transaction swt
                ON swt.workflow_transition_id = latest.max_id
               AND UPPER(COALESCE(swt.status, '')) = 'SEND_CALL_TO_IBS'
        INNER JOIN po_header ph2
                ON (ph2.po_no = CONVERT(sic.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
                 OR ph2.po_no = CONVERT(SUBSTRING_INDEX(sic.po_no, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci)
        LEFT JOIN (
            SELECT CONVERT(TRIM(call_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS call_no,
                   SUM(total_accepted) AS acc_nos,
                   SUM(accepted_sets_quantity) AS acc_sets
            FROM sleeper_final_result
            GROUP BY CONVERT(TRIM(call_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        ) sfr ON sfr.call_no = CONVERT(TRIM(sic.call_no) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN (
            SELECT CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS call_no, MIN(certificate_no) AS cert_no, MIN(created_on) AS created_on
            FROM sleeper_inspection_complete_details
            GROUP BY CONVERT(call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
        ) sicd ON sicd.call_no = CONVERT(sic.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci
        LEFT JOIN (SELECT CONVERT(TRIM(ic_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic_no, MAX(created_at) AS ic_dt FROM sleeper_final_ic_edit GROUP BY CONVERT(TRIM(ic_number) USING utf8mb4) COLLATE utf8mb4_unicode_ci) sfied ON sfied.ic_no = CONVERT(TRIM(COALESCE(sicd.cert_no, sic.call_no)) USING utf8mb4) COLLATE utf8mb4_unicode_ci
        WHERE ph2.item_cat_descr = 'PSC Mainline Sleeper'
    ) c
    GROUP BY c.ph_id, c.uom
) a ON a.ph_id = ph.id AND a.uom = pu.uom
WHERE ph.item_cat_descr = 'PSC Mainline Sleeper'
ORDER BY ph.po_no, CASE pu.uom WHEN 'Nos.' THEN 0 ELSE 1 END
""", nativeQuery = true)
  List<com.sarthi.Sleeper.dto.SleeperDashboardDtos.MprUomProjection> getMprDataByUom(
          @Param("startDate") LocalDate startDate,
          @Param("endDate") LocalDate endDate);

    /*
     * CM dashboard (Sleeper). For each call shown: lw = its latest workflow row, asg = the IEs/CMs it
     * was assigned to. Both are looked up per call through the request_id index (COLLATE is applied to
     * sic.call_no, never to request_id, so the index stays usable). The previous version grouped the
     * WHOLE workflow table on every request (19-79 s in prod). Columns / labels / rules are unchanged.
     */
    String SLEEPER_CM_LATEST_WF = """
        LEFT JOIN sleeper_workflow_transaction lw
          ON lw.workflow_transition_id = (
               SELECT MAX(w2.workflow_transition_id)
               FROM sleeper_workflow_transaction w2
               WHERE w2.request_id = sic.call_no COLLATE utf8mb4_0900_ai_ci)
        LEFT JOIN LATERAL (
            SELECT GROUP_CONCAT(DISTINCT CONCAT(ieu.full_name, ' - ', ieu.employee_code) SEPARATOR ', ') AS ie_names,
                   GROUP_CONCAT(DISTINCT CASE WHEN cmu.userid IS NOT NULL
                                              THEN CONCAT(cmu.employee_code, ' - ', cmu.full_name) END SEPARATOR ', ') AS cm_names,
                   MAX(CASE WHEN ieu.controlling_manager_id = :cmUserId THEN 1 ELSE 0 END) AS mine
            FROM sleeper_workflow_transaction wa
            JOIN user_master ieu ON ieu.userid = wa.assigned_to_user
            LEFT JOIN user_master cmu ON cmu.userid = ieu.controlling_manager_id
            WHERE wa.request_id = sic.call_no COLLATE utf8mb4_0900_ai_ci
        ) asg ON TRUE
        """;

    String SLEEPER_CM_STATUS = """
            CASE
                WHEN lw.action = 'CREATED' THEN 'Pending for Call Desk Verification'
                WHEN lw.action = 'VERIFY' THEN 'Pending - Assigned to IE'
                WHEN lw.action = 'MAIN_IE_SCHEDULE_CALL' THEN 'Pending - Schedule'
                WHEN lw.action IN ('INITIATE_CALL', 'PO_VERIFICATION', 'PAUSE') THEN 'Under Inspection'
                WHEN lw.action = 'SEND_CALL_TO_IBS' THEN 'Completed'
                WHEN lw.action IN ('CANCELLED', 'CANCEL') OR lw.status IN ('CANCELLED', 'CANCEL') THEN 'Completed (Cancelled)'
                WHEN lw.action IN ('WITHDRAW', 'WITHDRAWN') OR lw.status IN ('WITHDRAW', 'WITHDRAWN') THEN 'Completed (Withdrawn)'
                WHEN lw.action IN ('REJECTED', 'REJECT') OR lw.status IN ('REJECTED', 'REJECT') THEN 'Completed (Rejected)'
                WHEN (lw.action = 'FINISH' AND lw.status = 'COMPLETED') OR lw.action IN ('GENERATE_IC', 'DSC_SIGN_IC')
                    THEN 'Pending for IC Issue'
                WHEN lw.action IS NULL THEN NULL
                ELSE 'Under Inspection'
            END AS status
        """;

    @Query(value = """
        SELECT
            sic.call_no AS callNumber,
            'Sleeper - Final' AS productAndStageOfInspection,
            CONCAT(sic.po_no, '-', sic.sr_no) AS poNumber,
            (SELECT pi.delivery_date FROM po_header ph JOIN po_item pi ON pi.po_header_id = ph.id
              WHERE ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
                AND pi.item_sr_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.sr_no, '/', -1) COLLATE utf8mb4_unicode_ci
              LIMIT 1) AS deliveryDate,
            (SELECT pi.extended_delivery_date FROM po_header ph JOIN po_item pi ON pi.po_header_id = ph.id
              WHERE ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
                AND pi.item_sr_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.sr_no, '/', -1) COLLATE utf8mb4_unicode_ci
              LIMIT 1) AS expectedDeliveryDate,
            (SELECT ph.vendor_details FROM po_header ph
              WHERE ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci LIMIT 1) AS vendorName,
            NULL AS inspectionDesiredDate,
            sic.created_at AS callDate,
            COALESCE(asg.ie_names,
                (SELECT CONCAT(um.employee_code, ' - ', um.full_name)
                 FROM sleeper_poi_ie_mapping spim JOIN user_master um ON um.userid = spim.ie_user_id
                 WHERE spim.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id AND spim.ie_type = 'Main IE'
                 LIMIT 1)) AS ieName,
            asg.cm_names AS cmName,
            (SELECT ifm.rio
             FROM sleeper_pincode_poi_mapping sppm
             JOIN ie_fields_mapping ifm ON ifm.pin_code COLLATE utf8mb4_unicode_ci = sppm.pin_code AND ifm.product = 'Sleeper'
             WHERE CAST(sppm.vendor_code AS UNSIGNED) = sic.created_by
             LIMIT 1) AS ritesRio,
        """ + SLEEPER_CM_STATUS + """
        FROM sleeper_inspection_call sic
        """ + SLEEPER_CM_LATEST_WF + """
        WHERE sic.created_at BETWEEN :fromDate AND :toDate
          AND (:cmUserId = 0 OR asg.mine = 1)
        ORDER BY sic.created_at DESC
        """, nativeQuery = true)
    List<Object[]> getSleeperInspectionReport(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("cmUserId") Integer cmUserId
    );

    @Query(value = """
        SELECT
            sic.call_no AS callNumber,
            'Sleeper - Final' AS productAndStageOfInspection,
            CONCAT(sic.po_no, '-', sic.sr_no) AS poNumber,
            NULL AS deliveryDate,
            NULL AS expectedDeliveryDate,
            (SELECT ph.vendor_details FROM po_header ph
              WHERE ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci LIMIT 1) AS vendorName,
            sic.desired_inspection_date AS inspectionDesiredDate,
            sic.created_at AS callDate,
            COALESCE(asg.ie_names,
                (SELECT um.username
                 FROM sleeper_poi_ie_mapping spim JOIN user_master um ON um.userid = spim.ie_user_id
                 WHERE spim.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id AND spim.ie_type = 'Main IE'
                 LIMIT 1)) AS ieName,
            asg.cm_names AS cmName,
            (SELECT ifm.rio
             FROM sleeper_pincode_poi_mapping sppm
             JOIN ie_fields_mapping ifm ON ifm.pin_code COLLATE utf8mb4_unicode_ci = sppm.pin_code AND ifm.product = 'Sleeper'
             WHERE sppm.vendor_code COLLATE utf8mb4_unicode_ci = sic.created_by
             LIMIT 1) AS ritesRio,
        """ + SLEEPER_CM_STATUS + """
        FROM sleeper_inspection_call sic
        """ + SLEEPER_CM_LATEST_WF + """
        WHERE sic.created_at BETWEEN :fromDate AND :toDate
          AND (:cmUserId = 0 OR asg.mine = 1)
          AND sic.desired_inspection_date < CURDATE()
          AND lw.job_status = 'RIO_VERIFIED'
        ORDER BY sic.created_at DESC
        """, nativeQuery = true)
    List<Object[]> getSleeperOverduePendingCallsReport(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("cmUserId") Integer cmUserId
    );

    @Query(value = """

    WITH ies AS (
        SELECT um.userid, um.employee_code, um.username
        FROM user_master um
        JOIN user_master upcm ON upcm.userid = um.controlling_manager_id
        WHERE CONCAT(',', REPLACE(REPLACE(LOWER(TRIM(um.role_name)), ' ,', ','), ', ', ','), ',') REGEXP ',(main ie|sleeper ie|sleeper main ie|sleeper process ie),'
          AND upcm.employee_code = :cmEmployeeCode
    ),
    ie_calls AS (
        SELECT DISTINCT ies.userid, sic.id AS sic_id, sic.call_no, sic.desired_inspection_date
        FROM ies
        JOIN sleeper_poi_ie_mapping spim ON spim.ie_user_id = ies.userid
        JOIN sleeper_inspection_call sic
          ON sic.plant_id COLLATE utf8mb4_unicode_ci = spim.plant_id COLLATE utf8mb4_unicode_ci
    ),
    latest AS (
        SELECT c.call_no, swt.action, swt.job_status
        FROM (SELECT DISTINCT call_no FROM ie_calls) c
        JOIN sleeper_workflow_transaction swt
          ON swt.workflow_transition_id = (
               SELECT MAX(s2.workflow_transition_id)
               FROM sleeper_workflow_transaction s2
               WHERE s2.request_id = c.call_no COLLATE utf8mb4_0900_ai_ci)
    )
    SELECT
        ies.employee_code AS ieId,
        ies.username      AS ieName,
        COUNT(CASE WHEN l.action = 'VERIFY' THEN 1 END)                                     AS noOfCallsPending,
        COUNT(CASE WHEN l.action IN ('INITIATE_CALL','PO_VERIFICATION','PAUSE') THEN 1 END) AS noOfCallsUnderInspection,
        COUNT(CASE WHEN l.action = 'FINISH' THEN 1 END)                                     AS noOfCallsPendingForIc,
        COUNT(CASE WHEN l.job_status = 'RIO_VERIFIED'
                    AND ic.desired_inspection_date < CURDATE() THEN 1 END)                  AS noOfCallsOverdue
    FROM ies
    LEFT JOIN ie_calls ic ON ic.userid = ies.userid
    LEFT JOIN latest l    ON l.call_no = ic.call_no
    GROUP BY ies.userid, ies.employee_code, ies.username
    ORDER BY ies.employee_code

    """, nativeQuery = true)
    List<Object[]> getSleeperIeWiseCallStatusWorkloadSummary(
            @Param("cmEmployeeCode") String cmEmployeeCode
    );


    @Query(value = """

WITH ies AS (
    SELECT um.userid, um.employee_code, um.username
    FROM user_master um
    JOIN user_master upcm ON upcm.userid = um.controlling_manager_id
    WHERE CONCAT(',', REPLACE(REPLACE(LOWER(TRIM(um.role_name)), ' ,', ','), ', ', ','), ',') REGEXP ',(main ie|sleeper ie|sleeper main ie|sleeper process ie),'
      AND upcm.employee_code = :cmEmployeeCode
),
ie_plants AS (
    SELECT DISTINCT spim.ie_user_id, spim.plant_id
    FROM sleeper_poi_ie_mapping spim
    JOIN ies ON ies.userid = spim.ie_user_id
),
calls AS (
    SELECT sic.call_no, sic.plant_id, sic.desired_inspection_date
    FROM sleeper_inspection_call sic
    WHERE sic.plant_id COLLATE utf8mb4_unicode_ci IN (SELECT plant_id COLLATE utf8mb4_unicode_ci FROM ie_plants)
),
wf AS (
    SELECT c.call_no,
        (SELECT s.action FROM sleeper_workflow_transaction s
          WHERE s.workflow_transition_id = (SELECT MAX(s2.workflow_transition_id) FROM sleeper_workflow_transaction s2
                                            WHERE s2.request_id = c.call_no COLLATE utf8mb4_0900_ai_ci)) AS last_action,
        (SELECT s.status FROM sleeper_workflow_transaction s
          WHERE s.workflow_transition_id = (SELECT MAX(s2.workflow_transition_id) FROM sleeper_workflow_transaction s2
                                            WHERE s2.request_id = c.call_no COLLATE utf8mb4_0900_ai_ci)) AS last_status,
        (SELECT DATE(s.created_date) FROM sleeper_workflow_transaction s
          WHERE s.workflow_transition_id = (SELECT MIN(s2.workflow_transition_id) FROM sleeper_workflow_transaction s2
                                            WHERE s2.request_id = c.call_no COLLATE utf8mb4_0900_ai_ci
                                              AND s2.action = 'MAIN_IE_SCHEDULE_CALL')) AS schedule_date
    FROM (SELECT DISTINCT call_no FROM calls) c
),
fc AS (
    SELECT DISTINCT
        c.call_no,
        c.plant_id,
        CASE
            WHEN fcih.qty_offered_now = fcih.accepted_qty AND fcih.rejected_qty = 0 THEN 'ACCEPTED'
            WHEN fcih.qty_offered_now = fcih.rejected_qty THEN 'REJECTED'
            WHEN fcih.rejected_qty > 0 AND fcih.rejected_qty < fcih.qty_offered_now THEN 'PARTIAL'
            ELSE NULL
        END AS finalStatus,
        CASE WHEN c.desired_inspection_date < wf.schedule_date THEN 1 ELSE 0 END AS isOverdue,
        CASE WHEN wf.last_status = 'DSC_SIGN_IC' OR wf.last_action = 'SEND_CALL_TO_IBS' THEN 1 ELSE 0 END AS icIssued
    FROM calls c
    JOIN wf ON wf.call_no = c.call_no
    JOIN final_call_inspection_header fcih
      ON fcih.call_no COLLATE utf8mb4_unicode_ci = c.call_no COLLATE utf8mb4_unicode_ci
    WHERE wf.last_action IN ('FINISH', 'GENERATE_IC', 'IC_GENERATION', 'DSC_SIGN_IC', 'SEND_CALL_TO_IBS')
)
SELECT
    ies.employee_code AS ieId,
    ies.username      AS ieName,
    COUNT(DISTINCT fc.call_no) AS totalCalls,
    COUNT(DISTINCT CASE WHEN fc.isOverdue = 1 THEN fc.call_no END) AS overdueCallsAttended,
    COUNT(DISTINCT CASE WHEN fc.finalStatus = 'ACCEPTED' THEN fc.call_no END) AS callsAccepted,
    COUNT(DISTINCT CASE WHEN fc.finalStatus = 'REJECTED' THEN fc.call_no END) AS callsRejected,
    COUNT(DISTINCT CASE WHEN fc.finalStatus = 'PARTIAL' THEN fc.call_no END) AS callsPartiallyAcceptedRejected,
    COUNT(DISTINCT CASE WHEN fc.icIssued = 1 THEN fc.call_no END) AS icIssued
FROM ies
LEFT JOIN ie_plants ip ON ip.ie_user_id = ies.userid
LEFT JOIN fc ON fc.plant_id COLLATE utf8mb4_unicode_ci = ip.plant_id COLLATE utf8mb4_unicode_ci
GROUP BY ies.employee_code, ies.username
ORDER BY ies.employee_code

""", nativeQuery = true)
    List<Object[]> getSleeperIeOperationalSlaPerformanceSummary(
            @Param("cmEmployeeCode") String cmEmployeeCode
    );


    @Query(value = """

    SELECT

        COALESCE(
            (
                SELECT sicd.certificate_no
                FROM sleeper_inspection_complete_details sicd
                WHERE sicd.call_no COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci
                ORDER BY sicd.id DESC
                LIMIT 1
            ),
            CONCAT(
                'C/',
                sic.call_no,
                '/',
                COALESCE((SELECT um.SHORT_NAME FROM sleeper_workflow_transaction swt LEFT JOIN USER_MASTER um ON CAST(um.USERID AS CHAR) COLLATE utf8mb4_unicode_ci = CAST(swt.assigned_to_user AS CHAR) COLLATE utf8mb4_unicode_ci WHERE swt.request_id COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci ORDER BY swt.workflow_transition_id DESC LIMIT 1), 'NV')
            )
        ) AS certificateNo,

        COALESCE(
            (SELECT sfisc.book_no FROM sleeper_final_ic_save_changes sfisc WHERE sfisc.ic_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1),
            (SELECT sfie.book_no FROM sleeper_final_ic_edit sfie WHERE sfie.ic_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1)
        ) AS bookNo,

        COALESCE(
            (SELECT sfisc.set_no FROM sleeper_final_ic_save_changes sfisc WHERE sfisc.ic_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1),
            (SELECT sfie.set_no FROM sleeper_final_ic_edit sfie WHERE sfie.ic_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1)
        ) AS setNo,

        COALESCE(
            DATE_FORMAT(
                (SELECT sicd.created_on FROM sleeper_inspection_complete_details sicd WHERE sicd.call_no COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci ORDER BY sicd.id DESC LIMIT 1),
                '%d.%m.%Y'
            ),
            DATE_FORMAT(CURRENT_DATE(), '%d.%m.%Y')
        ) AS date,

        (
            SELECT COUNT(DISTINCT sfr_inst.id) + 1
            FROM sleeper_final_result sfr_inst
            JOIN sleeper_inspection_call sic_inst ON sic_inst.call_no COLLATE utf8mb4_unicode_ci = sfr_inst.call_number COLLATE utf8mb4_unicode_ci
            WHERE sic_inst.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
              AND LPAD(TRIM(COALESCE(sic_inst.sr_no, sfr_inst.sr_no)), 3, '0') COLLATE utf8mb4_unicode_ci = LPAD(SUBSTRING_INDEX(TRIM(sic.sr_no), '/', -1), 3, '0') COLLATE utf8mb4_unicode_ci
              AND sic_inst.id < sic.id
        ) AS offeredInstallmentNumber,

        (
            SELECT COUNT(DISTINCT sfr_inst.id) + 1
            FROM sleeper_final_result sfr_inst
            JOIN sleeper_inspection_call sic_inst ON sic_inst.call_no COLLATE utf8mb4_unicode_ci = sfr_inst.call_number COLLATE utf8mb4_unicode_ci
            WHERE sic_inst.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
              AND LPAD(TRIM(COALESCE(sic_inst.sr_no, sfr_inst.sr_no)), 3, '0') COLLATE utf8mb4_unicode_ci = LPAD(SUBSTRING_INDEX(TRIM(sic.sr_no), '/', -1), 3, '0') COLLATE utf8mb4_unicode_ci
              AND COALESCE(sfr_inst.total_accepted, 0) > 0
              AND sic_inst.id < sic.id
        ) AS passedInstallmentNumber,

        COALESCE(
            (
                SELECT CONCAT(
                    TRIM(spm2.company_name),
                    ',\n',
                    TRIM(spm2.address),
                    IF(spm2.district IS NOT NULL AND TRIM(spm2.district) <> '' AND spm2.address NOT LIKE CONCAT('%', TRIM(spm2.district), '%'), CONCAT(', ', TRIM(spm2.district)), ''),
                    IF(spm2.state IS NOT NULL AND TRIM(spm2.state) <> '' AND spm2.address NOT LIKE CONCAT('%', TRIM(spm2.state), '%'), CONCAT(', ', TRIM(spm2.state)), ''),
                    IF(UPPER(spm2.address) NOT LIKE '%INDIA%', ', INDIA', ''),
                    IF(spm2.pin_code IS NOT NULL AND TRIM(spm2.pin_code) <> '' AND spm2.address NOT LIKE CONCAT('%', TRIM(spm2.pin_code), '%'), CONCAT(' - ', TRIM(spm2.pin_code)), '')
                )
                FROM sleeper_pincode_poi_mapping spm2
                WHERE (spm2.vendor_code IS NOT NULL AND CONVERT(REPLACE(TRIM(spm2.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(COALESCE(ph.vendor_code, '')), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)
                   OR CONVERT(REPLACE(TRIM(spm2.poi_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(sic.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(spm2.poi_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(SUBSTRING_INDEX(TRIM(sic.plant_id), '/', 1), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR spm2.unit_name COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
                ORDER BY (CASE 
                    WHEN (spm2.vendor_code IS NOT NULL AND CONVERT(REPLACE(TRIM(spm2.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(COALESCE(ph.vendor_code, '')), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci) THEN 1
                    ELSE 2 END) ASC
                LIMIT 1
            ),
            (
                SELECT CONCAT(
                    TRIM(vp2.company_name),
                    ',\n',
                    TRIM(vp2.plant_address),
                    IF(vp2.plant_pincode IS NOT NULL AND TRIM(vp2.plant_pincode) <> '' AND vp2.plant_address NOT LIKE CONCAT('%', TRIM(vp2.plant_pincode), '%'),
                       IF(UPPER(vp2.plant_address) LIKE '%INDIA%', CONCAT(' - ', TRIM(vp2.plant_pincode)), CONCAT(', INDIA - ', TRIM(vp2.plant_pincode))),
                       '')
                )
                FROM vendor_plant vp2
                WHERE (vp2.vendor_code IS NOT NULL AND CONVERT(REPLACE(TRIM(vp2.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(COALESCE(ph.vendor_code, '')), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)
                   OR vp2.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(vp2.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(sic.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR vp2.plant_name COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
                ORDER BY (CASE 
                    WHEN (vp2.vendor_code IS NOT NULL AND CONVERT(REPLACE(TRIM(vp2.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(COALESCE(ph.vendor_code, '')), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci) THEN 1
                    ELSE 2 END) ASC
                LIMIT 1
            ),
            COALESCE(ph.vendor_details, ph.vendor_code)
        ) AS contractor,

        COALESCE(
            (
                SELECT CONCAT(
                    TRIM(vp.plant_name),
                    ',\n',
                    TRIM(vp.plant_address),
                    IF(vp.plant_pincode IS NOT NULL AND TRIM(vp.plant_pincode) <> '' AND vp.plant_address NOT LIKE CONCAT('%', TRIM(vp.plant_pincode), '%'),
                       IF(UPPER(vp.plant_address) LIKE '%INDIA%', CONCAT(' - ', TRIM(vp.plant_pincode)), CONCAT(', INDIA - ', TRIM(vp.plant_pincode))),
                       '')
                )
                FROM vendor_plant vp
                WHERE vp.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(vp.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(sic.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR vp.plant_name COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(vp.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(SUBSTRING_INDEX(TRIM(sic.plant_id), '/', 1), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR (vp.vendor_code IS NOT NULL AND CONVERT(REPLACE(TRIM(vp.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(COALESCE(ph.vendor_code, '')), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)
                ORDER BY (CASE 
                    WHEN vp.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci THEN 1
                    WHEN CONVERT(REPLACE(TRIM(vp.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(sic.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci THEN 2
                    WHEN vp.plant_name COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci THEN 3
                    ELSE 4 END) ASC
                LIMIT 1
            ),
            (
                SELECT CONCAT(
                    COALESCE(NULLIF(TRIM(spm.unit_name), ''), TRIM(spm.company_name)),
                    ',\n',
                    TRIM(spm.address),
                    IF(spm.district IS NOT NULL AND TRIM(spm.district) <> '' AND spm.address NOT LIKE CONCAT('%', TRIM(spm.district), '%'), CONCAT(', ', TRIM(spm.district)), ''),
                    IF(spm.state IS NOT NULL AND TRIM(spm.state) <> '' AND spm.address NOT LIKE CONCAT('%', TRIM(spm.state), '%'), CONCAT(', ', TRIM(spm.state)), ''),
                    IF(UPPER(spm.address) NOT LIKE '%INDIA%', ', INDIA', ''),
                    IF(spm.pin_code IS NOT NULL AND TRIM(spm.pin_code) <> '' AND spm.address NOT LIKE CONCAT('%', TRIM(spm.pin_code), '%'), CONCAT(' - ', TRIM(spm.pin_code)), '')
                )
                FROM sleeper_pincode_poi_mapping spm
                WHERE CONVERT(REPLACE(TRIM(spm.poi_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(sic.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(spm.poi_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(SUBSTRING_INDEX(TRIM(sic.plant_id), '/', 1), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR spm.unit_name COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
                   OR (spm.vendor_code IS NOT NULL AND CONVERT(REPLACE(TRIM(spm.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(COALESCE(ph.vendor_code, '')), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci)
                ORDER BY (CASE 
                    WHEN CONVERT(REPLACE(TRIM(spm.poi_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(REPLACE(TRIM(sic.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci THEN 1
                    WHEN spm.unit_name COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci THEN 2
                    ELSE 3 END) ASC
                LIMIT 1
            ),
            sic.plant_id,
            COALESCE(ph.vendor_details, ph.vendor_code)
        ) AS placeOfInspection,

        CONCAT(
            IF(COALESCE(NULLIF(TRIM(ph.rly_short_name), ''), NULLIF(TRIM(ph.rly_cd), '')) IS NOT NULL,
               CONCAT(COALESCE(NULLIF(TRIM(ph.rly_short_name), ''), NULLIF(TRIM(ph.rly_cd), '')), '/'),
               ''),
            IF(
                sic.po_no LIKE '%/%',
                TRIM(sic.po_no),
                CONCAT(
                    TRIM(sic.po_no),
                    IF(
                        sic.sr_no IS NOT NULL AND TRIM(sic.sr_no) <> '',
                        CONCAT('/', LPAD(SUBSTRING_INDEX(TRIM(sic.sr_no), '/', -1), 3, '0')),
                        IF(pi.item_sr_no IS NOT NULL AND TRIM(pi.item_sr_no) <> '', CONCAT('/', LPAD(TRIM(pi.item_sr_no), 3, '0')), '')
                    )
                )
            ),
            IF(ph.po_date IS NOT NULL, CONCAT(' dated ', DATE_FORMAT(ph.po_date, '%d.%m.%Y')), IF(sic.created_at IS NOT NULL, CONCAT(' dated ', DATE_FORMAT(sic.created_at, '%d.%m.%Y')), ''))
        ) AS contractRefAndDate,

        COALESCE(pi.bill_pay_off_desc, ph.bill_pay_off_name, ph.bill_pay_off) AS billPayingOffice,

        COALESCE(pi.consignee_detail, pi.imms_consignee_name, pi.consignee_cd, ph.purchaser_detail) AS consignee,

        ph.purchaser_detail AS purchasingAuthority,

        sic.sr_no AS itemNo,

        CONCAT(sic.call_no, '/', sic.sr_no, ' - ', COALESCE(pi.item_desc, 'MANUFACTURE AND SUPPLY OF PRESTRESSED MONO-BLOCK CONCRETE LINE SLEEPERES (RT-8746) (PRETENSIONED TYPE) FOR BROAD GAUGE(1673 MM)')) AS descriptionOfStores,

        CAST(COALESCE(pi.qty, 0) AS SIGNED) AS quantityOnOrder,

        CAST(COALESCE(
            IF(
                (UPPER(TRIM(COALESCE(pi.uom, ''))) LIKE '%SET%' 
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%PNC%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%TURNOUT%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4865%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-9790%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4218%'),
                (
                    SELECT SUM(COALESCE(sfr_prev.offered_sets_quantity, 0))
                    FROM sleeper_final_result sfr_prev
                    JOIN sleeper_inspection_call sic_prev ON sic_prev.call_no COLLATE utf8mb4_unicode_ci = sfr_prev.call_number COLLATE utf8mb4_unicode_ci
                    WHERE sic_prev.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
                      AND LPAD(TRIM(COALESCE(sic_prev.sr_no, sfr_prev.sr_no)), 3, '0') COLLATE utf8mb4_unicode_ci = LPAD(SUBSTRING_INDEX(TRIM(sic.sr_no), '/', -1), 3, '0') COLLATE utf8mb4_unicode_ci
                      AND sic_prev.id < sic.id
                ),
                (
                    SELECT SUM(sfr_prev.total_offered_quantity)
                    FROM sleeper_final_result sfr_prev
                    JOIN sleeper_inspection_call sic_prev ON sic_prev.call_no COLLATE utf8mb4_unicode_ci = sfr_prev.call_number COLLATE utf8mb4_unicode_ci
                    WHERE sic_prev.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
                      AND LPAD(TRIM(COALESCE(sic_prev.sr_no, sfr_prev.sr_no)), 3, '0') COLLATE utf8mb4_unicode_ci = LPAD(SUBSTRING_INDEX(TRIM(sic.sr_no), '/', -1), 3, '0') COLLATE utf8mb4_unicode_ci
                      AND sic_prev.id < sic.id
                )
            ),
            0
        ) AS SIGNED) AS cumulativeQtyOfferedPreviously,

        CAST(COALESCE(
            IF(
                (UPPER(TRIM(COALESCE(pi.uom, ''))) LIKE '%SET%' 
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%PNC%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%TURNOUT%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4865%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-9790%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4218%'),
                (
                    SELECT SUM(COALESCE(sfr_prev.accepted_sets_quantity, 0))
                    FROM sleeper_final_result sfr_prev
                    JOIN sleeper_inspection_call sic_prev ON sic_prev.call_no COLLATE utf8mb4_unicode_ci = sfr_prev.call_number COLLATE utf8mb4_unicode_ci
                    WHERE sic_prev.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
                      AND LPAD(TRIM(COALESCE(sic_prev.sr_no, sfr_prev.sr_no)), 3, '0') COLLATE utf8mb4_unicode_ci = LPAD(SUBSTRING_INDEX(TRIM(sic.sr_no), '/', -1), 3, '0') COLLATE utf8mb4_unicode_ci
                      AND sic_prev.id < sic.id
                ),
                (
                    SELECT SUM(sfr_prev.total_accepted)
                    FROM sleeper_final_result sfr_prev
                    JOIN sleeper_inspection_call sic_prev ON sic_prev.call_no COLLATE utf8mb4_unicode_ci = sfr_prev.call_number COLLATE utf8mb4_unicode_ci
                    WHERE sic_prev.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
                      AND LPAD(TRIM(COALESCE(sic_prev.sr_no, sfr_prev.sr_no)), 3, '0') COLLATE utf8mb4_unicode_ci = LPAD(SUBSTRING_INDEX(TRIM(sic.sr_no), '/', -1), 3, '0') COLLATE utf8mb4_unicode_ci
                      AND sic_prev.id < sic.id
                )
            ),
            0
        ) AS SIGNED) AS quantityPreviouslyPassed,

        CAST(COALESCE(
            IF(
                (UPPER(TRIM(COALESCE(pi.uom, ''))) LIKE '%SET%' 
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%PNC%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%TURNOUT%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4865%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-9790%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4218%'),
                (SELECT sfr.offered_sets_quantity FROM sleeper_final_result sfr WHERE sfr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1),
                (SELECT sfr.total_offered_quantity FROM sleeper_final_result sfr WHERE sfr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1)
            ),
            (SELECT (COALESCE(sfr2.total_accepted, 0) + COALESCE(sfr2.total_rejected, 0)) FROM sleeper_final_result sfr2 WHERE sfr2.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1),
            sic.total_offered,
            0
        ) AS SIGNED) AS qtyNowOffered,

        CAST(COALESCE(
            IF(
                (UPPER(TRIM(COALESCE(pi.uom, ''))) LIKE '%SET%' 
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%PNC%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%TURNOUT%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4865%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-9790%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4218%'),
                (SELECT sfr.accepted_sets_quantity FROM sleeper_final_result sfr WHERE sfr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1),
                (SELECT sfr.total_accepted FROM sleeper_final_result sfr WHERE sfr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1)
            ),
            fcih.accepted_qty,
            0
        ) AS SIGNED) AS qtyNowPassed,

        CAST(COALESCE(
            IF(
                (UPPER(TRIM(COALESCE(pi.uom, ''))) LIKE '%SET%' 
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%PNC%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%TURNOUT%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4865%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-9790%'
                 OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4218%'),
                (SELECT sfr.rejected_sets_quantity FROM sleeper_final_result sfr WHERE sfr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1),
                (SELECT sfr.total_rejected FROM sleeper_final_result sfr WHERE sfr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1)
            ),
            fcih.rejected_qty,
            0
        ) AS SIGNED) AS qtyNowRejected,

        CAST(GREATEST(0, (
            COALESCE(pi.qty, 0)
            -
            COALESCE(
                IF(
                    (UPPER(TRIM(COALESCE(pi.uom, ''))) LIKE '%SET%' 
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%PNC%'
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%TURNOUT%'
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4865%'
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-9790%'
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4218%'),
                    (
                        SELECT SUM(COALESCE(sfr_prev.accepted_sets_quantity, 0))
                        FROM sleeper_final_result sfr_prev
                        JOIN sleeper_inspection_call sic_prev ON sic_prev.call_no COLLATE utf8mb4_unicode_ci = sfr_prev.call_number COLLATE utf8mb4_unicode_ci
                        WHERE sic_prev.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
                          AND LPAD(TRIM(COALESCE(sic_prev.sr_no, sfr_prev.sr_no)), 3, '0') COLLATE utf8mb4_unicode_ci = LPAD(SUBSTRING_INDEX(TRIM(sic.sr_no), '/', -1), 3, '0') COLLATE utf8mb4_unicode_ci
                          AND sic_prev.id < sic.id
                    ),
                    (
                        SELECT SUM(sfr_prev.total_accepted)
                        FROM sleeper_final_result sfr_prev
                        JOIN sleeper_inspection_call sic_prev ON sic_prev.call_no COLLATE utf8mb4_unicode_ci = sfr_prev.call_number COLLATE utf8mb4_unicode_ci
                        WHERE sic_prev.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci
                          AND LPAD(TRIM(COALESCE(sic_prev.sr_no, sfr_prev.sr_no)), 3, '0') COLLATE utf8mb4_unicode_ci = LPAD(SUBSTRING_INDEX(TRIM(sic.sr_no), '/', -1), 3, '0') COLLATE utf8mb4_unicode_ci
                          AND sic_prev.id < sic.id
                    )
                ),
                0
            )
            -
            COALESCE(
                IF(
                    (UPPER(TRIM(COALESCE(pi.uom, ''))) LIKE '%SET%' 
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%PNC%'
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%TURNOUT%'
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4865%'
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-9790%'
                     OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4218%'),
                    (SELECT sfr.accepted_sets_quantity FROM sleeper_final_result sfr WHERE sfr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1),
                    (SELECT sfr.total_accepted FROM sleeper_final_result sfr WHERE sfr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci LIMIT 1)
                ),
                fcih.accepted_qty,
                0
            )
        )) AS SIGNED) AS qtyStillDue,

        IF(
            (UPPER(TRIM(COALESCE(pi.uom, ''))) LIKE '%SET%' 
             OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%PNC%'
             OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%TURNOUT%'
             OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4865%'
             OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-9790%'
             OR UPPER(TRIM(COALESCE(pi.item_desc, sic.sleeper_type, ''))) LIKE '%RT-4218%'),
            'SETS.',
            'NOS.'
        ) AS unit,

        CONCAT(
            DATE_FORMAT(COALESCE(sic.created_at, CURRENT_DATE()), '%d.%m.%Y'),
            ', Desired Date: ',
            DATE_FORMAT(COALESCE(sic.desired_inspection_date, sic.created_at, CURRENT_DATE()), '%d.%m.%Y')
        ) AS dateOfCall,

        1 AS noOfVisits,

        COALESCE(
            (
                SELECT DATE_FORMAT(sfr.date_of_inspection, '%d.%m.%Y')
                FROM sleeper_final_result sfr
                WHERE sfr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci
                LIMIT 1
            ),
            DATE_FORMAT(fcih.call_date, '%d.%m.%Y'),
            DATE_FORMAT(fcih.created_date, '%d.%m.%Y'),
            DATE_FORMAT(CURRENT_DATE(), '%d.%m.%Y')
        ) AS dateOfInspection,

        (
            SELECT GROUP_CONCAT(
                DISTINCT ibs2.batch_no
                SEPARATOR ', '
            )
            FROM ie_batch_summary ibs2
            WHERE ibs2.call_no COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci
        ) AS quantityNowPassedBatchNos,

        ph.case_no AS caseNo,

        COALESCE(
            (
                SELECT vpp.rio
                FROM vendor_plant vpp
                WHERE vpp.plant_id COLLATE utf8mb4_unicode_ci = sic.plant_id COLLATE utf8mb4_unicode_ci
                LIMIT 1
            ),
            (
                SELECT ifm.rio
                FROM sleeper_pincode_poi_mapping sppm
                JOIN ie_fields_mapping ifm
                    ON ifm.pin_code COLLATE utf8mb4_unicode_ci = sppm.pin_code COLLATE utf8mb4_unicode_ci
                   AND ifm.product COLLATE utf8mb4_unicode_ci = 'Sleeper'
                WHERE CONVERT(REPLACE(TRIM(sppm.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(CAST(sic.created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                LIMIT 1
            ),
            (
                SELECT swt.rio
                FROM sleeper_workflow_transaction swt
                WHERE swt.request_id COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci
                  AND swt.rio IS NOT NULL
                ORDER BY swt.workflow_transition_id DESC
                LIMIT 1
            ),
            ph.region_code
        ) AS rio,

        sic.plant_id AS plantId

    FROM sleeper_inspection_call sic

    LEFT JOIN po_header ph
        ON ph.po_no COLLATE utf8mb4_unicode_ci = sic.po_no COLLATE utf8mb4_unicode_ci

    LEFT JOIN po_item pi
        ON pi.po_header_id = ph.id
        AND (
            pi.item_sr_no COLLATE utf8mb4_unicode_ci = sic.sr_no COLLATE utf8mb4_unicode_ci
            OR pi.item_sr_no COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(sic.sr_no, '/', -1) COLLATE utf8mb4_unicode_ci
            OR CAST(pi.item_sr_no AS UNSIGNED) = CAST(SUBSTRING_INDEX(sic.sr_no, '/', -1) AS UNSIGNED)
            OR LPAD(pi.item_sr_no, 3, '0') COLLATE utf8mb4_unicode_ci = LPAD(SUBSTRING_INDEX(sic.sr_no, '/', -1), 3, '0') COLLATE utf8mb4_unicode_ci
        )

    LEFT JOIN final_call_inspection_header fcih
        ON fcih.call_no COLLATE utf8mb4_unicode_ci = sic.call_no COLLATE utf8mb4_unicode_ci

    WHERE sic.call_no COLLATE utf8mb4_unicode_ci = :callNo COLLATE utf8mb4_unicode_ci
    LIMIT 1

    """, nativeQuery = true)
    SleeperIcProjection getSleeperIcData(
            @Param("callNo") String callNo);

    @Query(value = """
            SELECT
                COALESCE(ph.case_no, '')                                AS caseNumber,
                DATE(sic.created_at)                                    AS callDate,
                COALESCE(CONVERT(pm.poi_code USING utf8mb4), CONVERT(sic.plant_id USING utf8mb4), CONVERT(sppm.poi_code USING utf8mb4)) AS placeOfInspection,
                COALESCE(CONVERT(pm.ibs_vendor_code USING utf8mb4), CONVERT(sic.plant_id USING utf8mb4)) AS ibsManufacturedCode,
                CAST(COALESCE(um_assigned.employee_code, um.employee_code, cd.created_by, wt.created_by, sic.created_by) AS CHAR) AS ieEmployeeNumber,
                'C'                                                     AS callStatus,
                'F'                                                     AS typeOfCall,
                (CASE
                    WHEN sic.po_no LIKE '%/%' THEN TRIM(sic.po_no)
                    WHEN sic.sr_no LIKE '%/%' THEN TRIM(sic.sr_no)
                    WHEN sic.sr_no IS NOT NULL AND TRIM(sic.sr_no) <> '' AND sic.po_no IS NOT NULL AND TRIM(sic.po_no) <> ''
                        THEN CONCAT(TRIM(sic.po_no), '/', TRIM(sic.sr_no))
                    WHEN sic.sr_no IS NOT NULL AND TRIM(sic.sr_no) <> '' THEN TRIM(sic.sr_no)
                    WHEN sic.po_no IS NOT NULL AND TRIM(sic.po_no) <> '' THEN TRIM(sic.po_no)
                    ELSE '1'
                END)                                                    AS poItemSerialNumber,
                ''                                                      AS bkNumber,
                ''                                                      AS setNumber,
                DATE(COALESCE(cd.created_date, wt.created_date, sic.created_at)) AS icDate,
                COALESCE(sic.total_offered, 0)                          AS quantityOffered,
                0                                                       AS quantityPassed,
                0                                                       AS quantityRejected,
                sic.call_no                                             AS callNo,
                CAST(NULL AS CHAR)                                      AS callNumber,
                COALESCE(cd.final_cancellation_charges, 0.0)            AS cancelCharges,
                COALESCE(NULLIF(TRIM(vp.rio), ''), NULLIF(TRIM(wt_assigned.rio), ''), '') AS plantRio
            FROM sleeper_inspection_call sic
            LEFT JOIN (
                SELECT DISTINCT call_number
                FROM sleeper_call_cancellation_details
            ) cd_exist ON cd_exist.call_number COLLATE utf8mb4_unicode_ci = sic.call_no
            LEFT JOIN (
                SELECT DISTINCT request_id
                FROM sleeper_workflow_transaction
                WHERE UPPER(COALESCE(status, '')) LIKE '%CANCEL%'
                   OR UPPER(COALESCE(job_status, '')) LIKE '%CANCEL%'
                   OR UPPER(COALESCE(status, '')) = 'BLOCKED'
                   OR UPPER(COALESCE(job_status, '')) = 'BLOCKED'
            ) wt_exist ON wt_exist.request_id COLLATE utf8mb4_unicode_ci = sic.call_no
            LEFT JOIN (
                SELECT swt1.workflow_transition_id, swt1.request_id, swt1.assigned_to_user, swt1.rio
                FROM sleeper_workflow_transaction swt1
                INNER JOIN (
                    SELECT request_id, MAX(workflow_transition_id) AS max_wt_id
                    FROM sleeper_workflow_transaction
                    WHERE assigned_to_user IS NOT NULL
                    GROUP BY request_id
                ) latest_wt1 ON swt1.workflow_transition_id = latest_wt1.max_wt_id
            ) wt_assigned ON wt_assigned.request_id COLLATE utf8mb4_unicode_ci = sic.call_no
            LEFT JOIN vendor_plant vp
                   ON CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sic.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(vp.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci =
                      CONVERT(REPLACE(TRIM(sic.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(vp.plant_id USING utf8mb4) COLLATE utf8mb4_unicode_ci =
                      CONVERT(SUBSTRING_INDEX(TRIM(sic.plant_id), '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(vp.plant_id), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci =
                      CONVERT(REPLACE(SUBSTRING_INDEX(TRIM(sic.plant_id), '/', 1), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(vp.vendor_code USING utf8mb4) COLLATE utf8mb4_unicode_ci =
                      CONVERT(SUBSTRING_INDEX(TRIM(sic.plant_id), '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(vp.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci =
                      CONVERT(REPLACE(SUBSTRING_INDEX(TRIM(sic.plant_id), '/', 1), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um_assigned
                   ON CONVERT(um_assigned.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(wt_assigned.assigned_to_user USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT sicd1.id, sicd1.call_no, sicd1.certificate_no
                FROM sleeper_inspection_complete_details sicd1
                INNER JOIN (
                    SELECT call_no, MAX(id) AS max_id
                    FROM sleeper_inspection_complete_details
                    GROUP BY call_no
                ) latest_sicd ON sicd1.id = latest_sicd.max_id
            ) sicd ON sicd.call_no COLLATE utf8mb4_unicode_ci = sic.call_no
            LEFT JOIN (
                SELECT cd_s.id, cd_s.call_number, cd_s.created_by, cd_s.created_date, cd_s.final_cancellation_charges
                FROM sleeper_call_cancellation_details cd_s
                INNER JOIN (
                    SELECT call_number, MAX(id) AS max_id
                    FROM sleeper_call_cancellation_details
                    GROUP BY call_number
                ) latest_cd ON cd_s.id = latest_cd.max_id
            ) cd ON cd.call_number COLLATE utf8mb4_unicode_ci = sic.call_no
            LEFT JOIN (
                SELECT swt2.workflow_transition_id, swt2.request_id, swt2.created_by, swt2.created_date
                FROM sleeper_workflow_transaction swt2
                INNER JOIN (
                    SELECT request_id, MAX(workflow_transition_id) AS max_wt_id
                    FROM sleeper_workflow_transaction
                    WHERE UPPER(COALESCE(status, '')) LIKE '%CANCEL%'
                       OR UPPER(COALESCE(job_status, '')) LIKE '%CANCEL%'
                       OR UPPER(COALESCE(status, '')) = 'BLOCKED'
                       OR UPPER(COALESCE(job_status, '')) = 'BLOCKED'
                    GROUP BY request_id
                ) latest_wt2 ON swt2.workflow_transition_id = latest_wt2.max_wt_id
            ) wt ON wt.request_id COLLATE utf8mb4_unicode_ci = sic.call_no
            LEFT JOIN po_header ph
                   ON CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci =
                      CONVERT((CASE WHEN sic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(sic.po_no, '/', 1) ELSE sic.po_no END) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um
                   ON CONVERT(um.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(cd.created_by, wt.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(cd.created_by, wt.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN sleeper_pincode_poi_mapping sppm
                   ON CONVERT(REPLACE(TRIM(sppm.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci =
                      CONVERT(SUBSTRING_INDEX(TRIM(sic.plant_id), '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(REPLACE(TRIM(sppm.vendor_code), ':', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci =
                      CONVERT(CAST(sic.created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN sarthi_ibs_poi_mapping pm
                   ON (
                       CONVERT(pm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(sppm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci
                       OR CONVERT(REPLACE(TRIM(pm.poi_code), ' ', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci =
                          CONVERT(REPLACE(TRIM(sic.plant_id), ' ', '') USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   )
                  AND pm.product_type = 'sleeper'
            LEFT JOIN (
                SELECT icr_s.id, icr_s.call_number, icr_s.status
                FROM ibs_call_registration icr_s
                INNER JOIN (
                    SELECT call_number, MAX(id) AS max_id
                    FROM ibs_call_registration
                    GROUP BY call_number
                ) latest_icr ON icr_s.id = latest_icr.max_id
            ) icr ON icr.call_number COLLATE utf8mb4_unicode_ci = sic.call_no
            WHERE (
                UPPER(COALESCE(sic.status, '')) LIKE '%CANCEL%'
                OR UPPER(COALESCE(sic.status, '')) = 'BLOCKED'
                OR cd_exist.call_number IS NOT NULL
                OR wt_exist.request_id IS NOT NULL
            )
            AND (
                icr.call_number IS NULL
                OR UPPER(icr.status) = 'FAILED'
            )
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
                cd.created_by,
                wt.created_by,
                sic.created_by,
                sic.po_no,
                sic.sr_no,
                cd.created_date,
                wt.created_date,
                sic.call_no,
                sicd.certificate_no,
                wt_assigned.rio,
                vp.rio,
                sic.total_offered,
                cd.final_cancellation_charges
            """, nativeQuery = true)
    List<Object[]> getSleeperCancelledInspectionCalls();

}
