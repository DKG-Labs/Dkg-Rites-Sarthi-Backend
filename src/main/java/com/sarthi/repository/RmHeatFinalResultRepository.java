package com.sarthi.repository;

import com.sarthi.entity.RmHeatFinalResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository for RmHeatFinalResult entity.
 */
@Repository
public interface RmHeatFinalResultRepository extends JpaRepository<RmHeatFinalResult, Long> {

    List<RmHeatFinalResult> findByInspectionCallNo(String inspectionCallNo);

    List<RmHeatFinalResult> findByInspectionCallNoAndHeatNo(String inspectionCallNo, String heatNo);

    List<RmHeatFinalResult> findByInspectionCallNoInAndHeatNo(List<String> inspectionCallNos, String heatNo);

    void deleteByInspectionCallNo(String inspectionCallNo);

    @Query("""
                SELECT COALESCE(SUM(r.acceptedQtyMt), 0)
                FROM RmHeatFinalResult r
                WHERE r.inspectionCallNo IN :callNos
                AND r.heatNo = :heatNo
            """)
    BigDecimal sumRmAcceptedQty(
            @Param("callNos") List<String> callNos,
            @Param("heatNo") String heatNo);

    @Query("""
                SELECT COALESCE(SUM(r.weightAcceptedMt), 0)
                FROM RmHeatFinalResult r
                WHERE r.inspectionCallNo IN :callNos
                AND r.heatNo = :heatNo
            """)
    BigDecimal sumWeightAcceptedMt(
            @Param("callNos") List<String> callNos,
            @Param("heatNo") String heatNo);

    /**
     * Sum offered earlier for a heat across multiple inspection calls
     */
    // @Query("""
    // SELECT COALESCE(SUM(r.offeredEarlier), 0)
    // FROM RmHeatFinalResult r
    // WHERE r.inspectionCallNo IN :callNos
    // AND r.heatNo = :heatNo
    // """)
    // Integer sumOfferedEarlierByHeatNoAndInspectionCallNos(
    // @Param("heatNo") String heatNo,
    // @Param("callNos") List<String> callNos
    // );

    @Query("""
                SELECT
                    SUM(r.totalQtyOfferedMt),
                    SUM(r.weightRejectedMt)
                FROM RmHeatFinalResult r
                WHERE r.inspectionCallNo IN :callNos
            """)
    List<Object[]> findOfferedAndRejectedByCallNos(
            @Param("callNos") List<String> callNos);

    /*
     * @Query("""
     * SELECT
     * SUM(r.acceptedQtyMt),
     * SUM(r.weightRejectedMt),
     * SUM(r.weightOfferedMt)
     * FROM RmHeatFinalResult r
     * WHERE r.inspectionCallNo IN :callNos
     * """)
     * List<Object[]> findRmSummaryByCallNos(
     *
     * @Param("callNos") List<String> callNos
     * );
     */
    /*
     * @Query("""
     * SELECT
     * r.inspectionCallNo,
     * SUM(r.totalQtyOfferedMt),
     * SUM(r.weightAcceptedMt),
     * SUM(r.weightRejectedMt)
     * FROM RmHeatFinalResult r
     * WHERE r.inspectionCallNo IN :callNos
     * GROUP BY r.inspectionCallNo
     * """)
     * List<Object[]> findRmSummaryByCallNos(
     *
     * @Param("callNos") List<String> callNos
     * );
     */
    // @Query("""
    // SELECT
    // r.inspectionCallNo,
    // r.totalQtyOfferedMt,
    // SUM(r.weightAcceptedMt),
    // SUM(r.weightRejectedMt)
    // FROM RmHeatFinalResult r
    // WHERE r.inspectionCallNo IN :callNos
    // GROUP BY r.inspectionCallNo
    // """)
    // List<Object[]> findRmSummaryByCallNos(
    // @Param("callNos") List<String> callNos);

    @Query("""
            SELECT
                r.inspectionCallNo,
                MAX(r.totalQtyOfferedMt),
                SUM(r.weightAcceptedMt),
                SUM(r.weightRejectedMt)
            FROM RmHeatFinalResult r
            WHERE r.inspectionCallNo IN :callNos
            GROUP BY r.inspectionCallNo
            """)
    List<Object[]> findRmSummaryByCallNos(
            @Param("callNos") List<String> callNos);

    /*
     * @Query(value = """
     * SELECT
     * p.id,
     * p.company_name,
     * p.poi_code,
     * u.username,
     * ip.rio,
     * 'Raw Material' AS stage,
     * SUM(r.total_qty_offered_mt),
     * SUM(r.accepted_qty_mt),
     * SUM(r.weight_rejected_mt)
     * FROM rm_heat_final_result r
     * JOIN inspection_calls ic ON ic.ic_number = r.inspection_call_no
     * JOIN pincode_poi_mapping p ON p.poi_code = ic.place_of_inspection
     * LEFT JOIN ie_pincode_poi_mapping ipm ON ipm.poi_code = p.poi_code AND
     * ipm.ie_type = 'PRIMARY'
     * LEFT JOIN ie_profile ip ON ip.employee_code = ipm.employee_code
     * LEFT JOIN po_header ph ON ph.po_no = ic.po_no
     * 
     * JOIN user_master u ON u.userid = r.created_by
     * 
     * WHERE (:startDate IS NULL OR DATE(r.created_at) >= :startDate)
     * AND (:endDate IS NULL OR DATE(r.created_at) <= :endDate)
     * AND (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
     * AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
     * AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)
     * GROUP BY
     * p.id,
     * p.company_name,
     * p.poi_code,
     * u.username,
     * ip.rio
     * """, countQuery = "SELECT COUNT(*) FROM pincode_poi_mapping", nativeQuery =
     * true)
     * Page<Object[]> fetchRaw(@Param("startDate") LocalDate startDate,
     * 
     * @Param("endDate") LocalDate endDate,
     * 
     * @Param("rio") String rio,
     * 
     * @Param("zone") String zone,
     * 
     * @Param("vendor") String vendor,
     * Pageable pageable);
     */
    /*
     * @Query(value = """
     * SELECT
     * p.id,
     * p.company_name,
     * p.poi_code,
     * u.username,
     * ip.rio,
     * 'Raw Material' AS stage,
     * 
     * (r.accepted_qty + r.rejected_qty) AS inspected_qty,
     * r.accepted_qty,
     * r.rejected_qty
     * 
     * FROM (
     * SELECT
     * inspection_call_no,
     * created_by,
     * 
     * SUM(COALESCE(accepted_qty_mt,0)) AS accepted_qty,
     * SUM(COALESCE(weight_rejected_mt,0)) AS rejected_qty
     * 
     * FROM rm_heat_final_result
     * WHERE (:startDate IS NULL OR DATE(created_at) >= :startDate)
     * AND (:endDate IS NULL OR DATE(created_at) <= :endDate)
     * 
     * GROUP BY inspection_call_no, created_by
     * ) r
     * 
     * JOIN inspection_calls ic ON ic.ic_number = r.inspection_call_no
     * JOIN pincode_poi_mapping p ON p.poi_code = ic.place_of_inspection
     * LEFT JOIN ie_pincode_poi_mapping ipm
     * ON ipm.poi_code = p.poi_code AND ipm.ie_type = 'PRIMARY'
     * LEFT JOIN ie_profile ip
     * ON ip.employee_code = ipm.employee_code
     * LEFT JOIN po_header ph
     * ON ph.po_no = ic.po_no
     * JOIN user_master u
     * ON u.userid = r.created_by
     * 
     * WHERE (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
     * AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
     * AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)
     * 
     * GROUP BY
     * p.id,
     * p.company_name,
     * p.poi_code,
     * u.username,
     * ip.rio,
     * r.accepted_qty,
     * r.rejected_qty
     * """,
     * countQuery = "SELECT COUNT(*) FROM rm_heat_final_result",
     * nativeQuery = true)
     * Page<Object[]> fetchRaw(
     * 
     * @Param("startDate") LocalDate startDate,
     * 
     * @Param("endDate") LocalDate endDate,
     * 
     * @Param("rio") String rio,
     * 
     * @Param("zone") String zone,
     * 
     * @Param("vendor") String vendor,
     * Pageable pageable);
     */
    /*
     * @Query(value = """
     * SELECT
     * p.id,
     * p.company_name,
     * p.poi_code,
     * u.username,
     * ip.rio,
     * 'Raw Material' AS stage,
     * 
     * (SUM(r.accepted_qty) + SUM(r.rejected_qty)) AS inspected_qty,
     * SUM(r.accepted_qty) AS accepted_qty,
     * SUM(r.rejected_qty) AS rejected_qty
     * 
     * FROM (
     * SELECT
     * inspection_call_no,
     * created_by,
     * SUM(COALESCE(accepted_qty_mt,0)) AS accepted_qty,
     * SUM(COALESCE(weight_rejected_mt,0)) AS rejected_qty
     * FROM rm_heat_final_result
     * WHERE (:startDate IS NULL OR DATE(created_at) >= :startDate)
     * AND (:endDate IS NULL OR DATE(created_at) <= :endDate)
     * GROUP BY inspection_call_no, created_by
     * ) r
     * 
     * JOIN inspection_calls ic
     * ON ic.ic_number = r.inspection_call_no
     * 
     * JOIN pincode_poi_mapping p
     * ON p.poi_code = ic.place_of_inspection
     * 
     * JOIN ie_pincode_poi_mapping ipm
     * ON ipm.poi_code = p.poi_code
     * AND ipm.ie_type = 'PRIMARY'
     * 
     * JOIN ie_profile ip
     * ON ip.employee_code = ipm.employee_code
     * 
     * JOIN user_master u
     * ON u.employee_code = ip.employee_code
     * 
     * LEFT JOIN po_header ph
     * ON ph.po_no = ic.po_no
     * 
     * 
     * WHERE (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
     * AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
     * AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)
     * AND r.created_by = u.userid
     * 
     * GROUP BY
     * p.id,
     * p.company_name,
     * p.poi_code,
     * u.username,
     * ip.rio
     * """,
     * countQuery = "SELECT COUNT(*) FROM rm_heat_final_result",
     * nativeQuery = true)
     * Page<Object[]> fetchRaw(
     * 
     * @Param("startDate") LocalDate startDate,
     * 
     * @Param("endDate") LocalDate endDate,
     * 
     * @Param("rio") String rio,
     * 
     * @Param("zone") String zone,
     * 
     * @Param("vendor") String vendor,
     * Pageable pageable);
     */
    @Query(value = """
            SELECT
                MAX(p.id) AS id,
                p.company_name,
                p.poi_code,
                u.username,
                ip.rio,
                'Raw Material' AS stage,

                SUM(r.accepted_qty + r.rejected_qty) AS inspected_qty,
                SUM(r.accepted_qty) AS accepted_qty,
                SUM(r.rejected_qty) AS rejected_qty

            FROM (

                SELECT
                    inspection_call_no,
                    created_by,
                    SUM(COALESCE(accepted_qty_mt,0)) AS accepted_qty,
                    SUM(COALESCE(weight_rejected_mt,0)) AS rejected_qty
                FROM rm_heat_final_result
                WHERE (:startDate IS NULL OR DATE(created_at) >= :startDate)
                  AND (:endDate IS NULL OR DATE(created_at) <= :endDate)
                GROUP BY inspection_call_no, created_by
            ) r


            JOIN inspection_calls ic
                ON ic.ic_number = r.inspection_call_no

            JOIN pincode_poi_mapping p
                ON p.poi_code = ic.place_of_inspection


            JOIN user_master u
                ON u.userid = r.created_by

            JOIN ie_profile ip
                ON ip.employee_code = u.employee_code

            LEFT JOIN po_header ph
                ON ph.po_no = ic.po_no

            WHERE (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
              AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
              AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)


            GROUP BY
                p.company_name,
                p.poi_code,
                u.username,
                ip.rio
            """, countQuery = """
            SELECT COUNT(DISTINCT inspection_call_no, created_by)
            FROM rm_heat_final_result
            """, nativeQuery = true)
    Page<Object[]> fetchRaw(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("rio") String rio,
            @Param("zone") String zone,
            @Param("vendor") String vendor,
            Pageable pageable);

    /*
     * 
     * @Query(value = """
     * SELECT
     * p.id,
     * p.company_name,
     * p.poi_code,
     * u.username,
     * ip.rio,
     * 'Raw Material' AS stage,
     * 
     * SUM(r.weight_accepted_mt + r.weight_rejected_mt) AS inspected_qty,
     * SUM(r.weight_accepted_mt) AS accepted_qty,
     * SUM(r.weight_rejected_mt) AS rejected_qty
     * 
     * FROM rm_heat_final_result r
     * JOIN inspection_calls ic ON ic.ic_number = r.inspection_call_no
     * JOIN pincode_poi_mapping p ON p.poi_code = ic.place_of_inspection
     * LEFT JOIN ie_pincode_poi_mapping ipm
     * ON ipm.poi_code = p.poi_code AND ipm.ie_type = 'PRIMARY'
     * LEFT JOIN ie_profile ip
     * ON ip.employee_code = ipm.employee_code
     * LEFT JOIN po_header ph
     * ON ph.po_no = ic.po_no
     * 
     * JOIN user_master u
     * ON u.userid = r.created_by
     * 
     * WHERE (:startDate IS NULL OR DATE(r.created_at) >= :startDate)
     * AND (:endDate IS NULL OR DATE(r.created_at) <= :endDate)
     * AND (:rio IS NULL OR :rio = '' OR UPPER(ip.rio) = UPPER(:rio))
     * AND (:zone IS NULL OR :zone = '' OR ph.rly_short_name = :zone)
     * AND (:vendor IS NULL OR :vendor = '' OR p.company_name = :vendor)
     * 
     * GROUP BY
     * p.id,
     * p.company_name,
     * p.poi_code,
     * u.username,
     * ip.rio
     * """,
     * countQuery = "SELECT COUNT(*) FROM pincode_poi_mapping",
     * nativeQuery = true)
     * Page<Object[]> fetchRaw(
     * 
     * @Param("startDate") LocalDate startDate,
     * 
     * @Param("endDate") LocalDate endDate,
     * 
     * @Param("rio") String rio,
     * 
     * @Param("zone") String zone,
     * 
     * @Param("vendor") String vendor,
     * Pageable pageable);
     * 
     */
    @Query("SELECT SUM(r.weightRejectedMt), SUM(r.weightOfferedMt) FROM RmHeatFinalResult r WHERE r.createdAt >= :date")
    List<Object[]> sumRmRejectionLast30Days(@Param("date") java.time.LocalDateTime date);

    @Query(value = """
                SELECT
                    SUM(COALESCE(r.weight_rejected_mt, 0)),
                    SUM(COALESCE(r.weight_offered_mt, 0))
                FROM rm_heat_final_result r
                LEFT JOIN inspection_calls ic ON r.inspection_call_no = ic.ic_number
                LEFT JOIN po_header ph ON ic.po_no = ph.po_no
                WHERE (CASE WHEN r.date_of_inspection IS NOT NULL THEN DATE(r.date_of_inspection) ELSE DATE(r.created_at) END) BETWEEN :startDate AND :endDate
                AND (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
                AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
            """, nativeQuery = true)
    List<Object[]> sumRmRejectionWithFilters(
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway);

    @Query(value = """
            SELECT
                ic.company_name AS name,
                (SUM(r.weight_rejected_mt) * 100.0 / NULLIF(SUM(r.weight_offered_mt), 0)) AS rejectionPct
            FROM rm_heat_final_result r
            JOIN inspection_calls ic ON ic.ic_number = r.inspection_call_no
            WHERE r.created_at >= :date
            GROUP BY ic.company_name
            ORDER BY rejectionPct DESC
            LIMIT 5
            """, nativeQuery = true)
    List<Object[]> findTop5ManufacturerRejection(@Param("date") java.time.LocalDateTime date);

    @Query("SELECT COALESCE(SUM(r.acceptedQtyMt), 0), COALESCE(SUM(r.weightRejectedMt), 0) FROM RmHeatFinalResult r")
    List<Object[]> sumRmAcceptedAndRejected();

    @Query(value = """
                SELECT
                    (
                        SELECT COALESCE(SUM(r.accepted_qty_mt), 0)
                        FROM inspection_calls ic
                        INNER JOIN po_header ph ON ic.po_no = ph.po_no
                        INNER JOIN rm_heat_final_result r ON r.inspection_call_no = ic.ic_number
                        INNER JOIN (
                            SELECT w.REQUESTID, w.STATUS
                            FROM WORKFLOW_TRANSITION w
                            INNER JOIN (
                                SELECT REQUESTID, MAX(WORKFLOWTRANSITIONID) AS max_id
                                FROM WORKFLOW_TRANSITION
                                GROUP BY REQUESTID
                            ) latest ON w.REQUESTID = latest.REQUESTID AND w.WORKFLOWTRANSITIONID = latest.max_id
                        ) wf ON wf.REQUESTID = ic.ic_number
                        WHERE (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
                        AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
                        AND wf.STATUS = 'SEND_CALL_TO_IBS'
                        AND (CASE WHEN r.date_of_inspection IS NOT NULL THEN DATE(r.date_of_inspection) ELSE DATE(r.created_at) END) BETWEEN :startDate AND :endDate
                    ),
                    (
                        SELECT COALESCE(SUM(sub.no_of_erc_finished), 0)
                        FROM (
                            SELECT 
                                r.inspection_call_no,
                                COALESCE(MAX(r.no_of_erc_finished), 0) AS no_of_erc_finished
                            FROM inspection_calls ic
                            INNER JOIN po_header ph ON ic.po_no = ph.po_no
                            INNER JOIN rm_heat_final_result r ON r.inspection_call_no = ic.ic_number
                            INNER JOIN (
                                SELECT w.REQUESTID, w.STATUS
                                FROM WORKFLOW_TRANSITION w
                                INNER JOIN (
                                    SELECT REQUESTID, MAX(WORKFLOWTRANSITIONID) AS max_id
                                    FROM WORKFLOW_TRANSITION
                                    GROUP BY REQUESTID
                                ) latest ON w.REQUESTID = latest.REQUESTID AND w.WORKFLOWTRANSITIONID = latest.max_id
                            ) wf ON wf.REQUESTID = ic.ic_number
                            WHERE (:vendorPlantCode IS NULL OR :vendorPlantCode = '' OR ic.place_of_inspection = :vendorPlantCode)
                            AND (:zonalRailway IS NULL OR :zonalRailway = '' OR ph.rly_short_name = :zonalRailway)
                            AND wf.STATUS = 'SEND_CALL_TO_IBS'
                            AND (UPPER(TRIM(r.overall_status)) = 'REJECTED' OR UPPER(TRIM(r.status)) = 'REJECTED')
                            AND (CASE WHEN r.date_of_inspection IS NOT NULL THEN DATE(r.date_of_inspection) ELSE DATE(r.created_at) END) BETWEEN :startDate AND :endDate
                            GROUP BY r.inspection_call_no
                        ) sub
                    )
            """, nativeQuery = true)
    List<Object[]> sumRmAcceptedAndRejectedRevisedLogic(
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate,
            @Param("vendorPlantCode") String vendorPlantCode,
            @Param("zonalRailway") String zonalRailway);

    List<RmHeatFinalResult> findByInspectionCallNoIn(List<String> callNos);

    @Query("""
            SELECT
                r.inspectionCallNo,
                SUM(
                    CASE
                        WHEN UPPER(r.dimensionalStatus) = 'NOT OK'
                        THEN r.weightRejectedMt
                        ELSE 0
                    END
                ),
                SUM(r.weightOfferedMt)
            FROM RmHeatFinalResult r
            WHERE r.inspectionCallNo IN :callNos
            GROUP BY r.inspectionCallNo
            """)
    List<Object[]> getHeatSummary(
            @Param("callNos") List<String> callNos);

    /*
     * @Query(value = """
     * 
     * SELECT
     * ph.case_no AS caseNumber,
     * 
     * DATE(ic.created_at) AS callDate,
     * 
     * ic.place_of_inspection AS placeOfInspection,
     * 
     * CAST(um.employee_code AS CHAR) AS ieEmployeeNumber,
     * 
     * 'IC Generated' AS callStatus,
     * 
     * ic.po_serial_no AS poItemSerialNumber,
     * 
     * CAST(rm.book_no AS CHAR) AS bkNumber,
     * 
     * CAST(rm.set_no AS CHAR) AS setNumber,
     * 
     * DATE(rm.created_at) AS icDate,
     * 
     * COALESCE(SUM(DISTINCT rmr.total_qty_offered_mt),0)
     * AS quantityOffered,
     * 
     * COALESCE(SUM(rmr.accepted_qty_mt),0)
     * AS quantityPassed,
     * 
     * COALESCE(SUM(rmr.weight_rejected_mt),0)
     * AS quantityRejected,
     * ic.ic_number AS callNo
     * 
     * 
     * FROM rm_ic_edit rm
     * 
     * INNER JOIN inspection_calls ic
     * ON ic.ic_number =
     * SUBSTRING_INDEX(
     * SUBSTRING_INDEX(rm.ic_number,'/',2),
     * '/',
     * -1
     * )
     * 
     * INNER JOIN po_header ph
     * ON ph.po_no = ic.po_no
     * 
     * INNER JOIN user_master um
     * ON um.userid = rm.created_by
     * 
     * LEFT JOIN rm_heat_final_result rmr
     * ON rmr.inspection_call_no = ic.ic_number
     * 
     * LEFT JOIN ibs_call_registration icr
     * ON icr.call_number = ic.ic_number
     * 
     * WHERE icr.call_number IS NULL
     * OR icr.status = 'Failed'
     * 
     * GROUP BY
     * ph.case_no,
     * ic.created_at,
     * ic.place_of_inspection,
     * um.employee_code,
     * ic.po_serial_no,
     * rm.book_no,
     * rm.set_no,
     * rm.created_at,ic.ic_number
     * 
     * """, nativeQuery = true)
     * List<Object[]> getRmInspectionCalls();
     */
    @Query(value = """
            SELECT
                COALESCE(ph.case_no, '')                                AS caseNumber,
                DATE(ic.created_at)                                     AS callDate,
                COALESCE(ic.place_of_inspection, '')                    AS placeOfInspection,
                COALESCE(pm.ibs_vendor_code, ic.place_of_inspection)    AS ibsManufacturedCode,
                CAST(COALESCE(um_wt.employee_code, um_rm.employee_code, um_ic.employee_code, wt_latest.assigned_to_user, wt_latest.createdby, rm.created_by, rmsc.created_by, ic.created_by) AS CHAR) AS ieEmployeeNumber,
                'A'                                                     AS callStatus,
                'S'                                                     AS typeOfCall,
                (CASE 
                    WHEN ic.po_no LIKE '%/%' AND SUBSTRING_INDEX(ic.po_no, '/', -1) <> '' THEN SUBSTRING_INDEX(ic.po_no, '/', -1)
                    WHEN ic.po_serial_no IS NOT NULL AND TRIM(ic.po_serial_no) <> '' THEN TRIM(ic.po_serial_no)
                    ELSE '1'
                END)                                                    AS poItemSerialNumber,
                CAST(COALESCE(rm.book_no, rmsc.book_no, '') AS CHAR)    AS bkNumber,
                CAST(COALESCE(rm.set_no, rmsc.set_no, '') AS CHAR)      AS setNumber,
                DATE(COALESCE(rm.created_at, rmsc.created_at, icd.created_on, ic.updated_at, ic.created_at)) AS icDate,
                COALESCE(rmr.offered_qty, (SELECT COALESCE(rmd.tc_quantity, rmd.total_offered_qty_mt, rmd.offered_qty_erc) FROM rm_inspection_details rmd WHERE rmd.ic_id = ic.id ORDER BY rmd.id DESC LIMIT 1), 0) AS quantityOffered,
                COALESCE(rmr.total_accepted, 0)                         AS quantityPassed,
                COALESCE(rmr.total_rejected, 0)                         AS quantityRejected,
                ic.ic_number                                            AS callNo,
                COALESCE(
                    NULLIF(icd.certificate_no, ''),
                    (CASE WHEN rm.ic_number LIKE '%/%' THEN rm.ic_number ELSE NULL END),
                    (CASE WHEN rmsc.ic_number LIKE '%/%' THEN rmsc.ic_number ELSE NULL END),
                    NULLIF(rm.ic_number, ''),
                    NULLIF(rmsc.ic_number, ''),
                    ic.ic_number
                )                                                       AS callNumber,
                0.0                                                     AS cancelCharges,
                0.0                                                     AS rejectCharges,
                COALESCE(NULLIF(TRIM(wt_latest.rio), ''), '')           AS plantRio
            FROM inspection_calls ic
            LEFT JOIN rm_ic_edit rm
                   ON CONVERT(rm.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(SUBSTRING_INDEX(SUBSTRING_INDEX(rm.ic_number, '/', 2), '/', -1) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(SUBSTRING_INDEX(rm.ic_number, '/', 1) USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(rm.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%')
            LEFT JOIN rm_ic_save_changes rmsc
                   ON CONVERT(rmsc.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT icd1.call_no, icd1.certificate_no, icd1.created_on
                FROM inspection_complete_details icd1
                INNER JOIN (
                    SELECT call_no, MAX(id) AS max_id
                    FROM inspection_complete_details
                    GROUP BY call_no
                ) latest_icd ON icd1.id = latest_icd.max_id
            ) icd ON CONVERT(icd.call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN po_header ph
                   ON CONVERT(ph.po_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = 
                      CONVERT((CASE WHEN ic.po_no LIKE '%/%' THEN SUBSTRING_INDEX(ic.po_no, '/', 1) ELSE ic.po_no END) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN sarthi_ibs_poi_mapping pm
                   ON CONVERT(pm.poi_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.place_of_inspection USING utf8mb4) COLLATE utf8mb4_unicode_ci
                  AND pm.product_type = 'erc'
            LEFT JOIN (
                SELECT wt1.requestid, wt1.status, wt1.action, wt1.job_status, wt1.createdby, wt1.assigned_to_user, wt1.rio
                FROM workflow_transition wt1
                INNER JOIN (
                    SELECT requestid, MAX(workflowtransitionid) AS max_wt_id
                    FROM workflow_transition
                    GROUP BY requestid
                ) latest_wt ON wt1.workflowtransitionid = latest_wt.max_wt_id
            ) wt_latest
                   ON CONVERT(wt_latest.requestid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR (rm.ic_number IS NOT NULL AND CONVERT(wt_latest.requestid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(rm.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci)
            LEFT JOIN user_master um_wt
                   ON CONVERT(um_wt.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(wt_latest.assigned_to_user, wt_latest.createdby) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um_wt.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(wt_latest.assigned_to_user, wt_latest.createdby) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um_rm
                   ON CONVERT(um_rm.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(rm.created_by, rmsc.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um_rm.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(COALESCE(rm.created_by, rmsc.created_by) USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN user_master um_ic
                   ON CONVERT(um_ic.userid USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
                   OR CONVERT(um_ic.employee_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.created_by USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT 
                    rmr_sub.inspection_call_no,
                    SUM(COALESCE(rmr_sub.total_qty_offered_mt, 0)) AS offered_qty,
                    SUM(COALESCE(rmr_sub.weight_accepted_mt, 0)) AS total_accepted,
                    SUM(COALESCE(rmr_sub.weight_rejected_mt, 0)) AS total_rejected
                FROM rm_heat_final_result rmr_sub
                GROUP BY rmr_sub.inspection_call_no
            ) rmr ON CONVERT(rmr.inspection_call_no USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            LEFT JOIN (
                SELECT icr1.*
                FROM ibs_call_registration icr1
                INNER JOIN (
                    SELECT call_number, MAX(version) AS max_version
                    FROM ibs_call_registration
                    GROUP BY call_number
                ) latest ON CONVERT(latest.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(icr1.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
                       AND latest.max_version = icr1.version
            ) icr ON CONVERT(icr.call_number USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(ic.ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci
            WHERE (
                UPPER(COALESCE(ic.type_of_call, '')) LIKE '%RAW%' 
                OR UPPER(COALESCE(ic.type_of_call, '')) LIKE '%RM%' 
                OR UPPER(COALESCE(ic.type_of_call, '')) = 'S' 
                OR UPPER(COALESCE(ic.ic_number, '')) LIKE 'ER%'
            )
            AND (
                UPPER(COALESCE(ic.status, '')) LIKE '%SEND_CALL_TO_IBS%'
                OR UPPER(COALESCE(ic.status, '')) LIKE '%SENT_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.status, '')) LIKE '%SEND_CALL_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.action, '')) LIKE '%SEND_CALL_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.status, '')) LIKE '%SENT_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.action, '')) LIKE '%SENT_TO_IBS%'
                OR UPPER(COALESCE(wt_latest.action, '')) LIKE '%SEND CALL TO IBS%'
            )
            AND (
                icr.call_number IS NULL
                OR UPPER(icr.status) = 'FAILED'
            )
            GROUP BY
                ph.case_no,
                ic.created_at,
                ic.place_of_inspection,
                pm.ibs_vendor_code,
                um_wt.employee_code,
                um_rm.employee_code,
                um_ic.employee_code,
                wt_latest.assigned_to_user,
                wt_latest.createdby,
                rm.created_by,
                rmsc.created_by,
                ic.created_by,
                ic.po_no,
                ic.po_serial_no,
                rm.book_no,
                rmsc.book_no,
                rm.set_no,
                rmsc.set_no,
                rm.created_at,
                rmsc.created_at,
                icd.created_on,
                ic.updated_at,
                ic.ic_number,
                icd.certificate_no,
                rm.ic_number,
                rmsc.ic_number,
                rmr.offered_qty,
                rmr.total_accepted,
                rmr.total_rejected,
                wt_latest.rio,
                ic.id
            """, nativeQuery = true)
    List<Object[]> getRmInspectionCalls();

    /** Bulk fetch: SUM(accepted_qty_mt) per ic_number for a list of RM call numbers */
    @Query(value = """
        SELECT r.inspection_call_no, COALESCE(SUM(r.accepted_qty_mt), 0)
        FROM rm_heat_final_result r
        WHERE r.inspection_call_no IN :icNumbers
        GROUP BY r.inspection_call_no
        """, nativeQuery = true)
    List<Object[]> sumAcceptedQtyByIcNumbers(@Param("icNumbers") List<String> icNumbers);

    @Query(value = """
        SELECT
            DATE_FORMAT(IFNULL(r.date_of_inspection, r.created_at), '%b-%y') AS Month_Year,
            SUM(COALESCE(r.weight_rejected_mt, 0)) AS Total_Rejected,
            SUM(COALESCE(r.weight_offered_mt, 0)) AS Total_Offered
        FROM rm_heat_final_result r
        WHERE IFNULL(r.date_of_inspection, r.created_at) BETWEEN :startDate AND :endDate
        GROUP BY
            YEAR(IFNULL(r.date_of_inspection, r.created_at)),
            MONTH(IFNULL(r.date_of_inspection, r.created_at)),
            Month_Year
        ORDER BY MIN(IFNULL(r.date_of_inspection, r.created_at)) ASC
        """, nativeQuery = true)
    List<Object[]> findMonthlyRmRejectionTrend(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}