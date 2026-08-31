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
                        AND wf.STATUS IN ('INSPECTION_COMPLETE_CONFIRM', 'GENERATE_IC', 'DSC_SIGN_IC')
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
                            AND wf.STATUS IN ('INSPECTION_COMPLETE_CONFIRM', 'GENERATE_IC', 'DSC_SIGN_IC')
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
                ph.case_no                                  AS caseNumber,

                DATE(ic.created_at)                         AS callDate,

                ic.place_of_inspection                      AS placeOfInspection,
                pm.ibs_vendor_code                          AS ibsManufacturedCode,

                CAST(um.employee_code AS CHAR)              AS ieEmployeeNumber,

                'A'                                         AS callStatus,
                'S'                                         AS typeOfCall,
                ic.po_serial_no                             AS poItemSerialNumber,

                CAST(rm.book_no AS CHAR)                    AS bkNumber,

                CAST(rm.set_no AS CHAR)                     AS setNumber,

                DATE(rm.created_at)                         AS icDate,

                COALESCE(SUM(DISTINCT rmr.total_qty_offered_mt), 0)
                                                            AS quantityOffered,

                COALESCE(SUM(rmr.weight_accepted_mt), 0)
                                                            AS quantityPassed,

                COALESCE(SUM(rmr.weight_rejected_mt), 0)
                                                            AS quantityRejected,

                ic.ic_number                                AS callNo,
                rm.ic_number                                AS callNumber

            FROM rm_ic_edit rm

            INNER JOIN inspection_calls ic
                    ON ic.ic_number COLLATE utf8mb4_unicode_ci =
                       SUBSTRING_INDEX(
                            SUBSTRING_INDEX(rm.ic_number, '/', 2),
                            '/',
                            -1
                       ) COLLATE utf8mb4_unicode_ci

            INNER JOIN po_header ph
                    ON ph.po_no = ic.po_no

            INNER JOIN user_master um
                    ON um.userid = rm.created_by

            LEFT JOIN rm_heat_final_result rmr
                    ON rmr.inspection_call_no COLLATE utf8mb4_unicode_ci =
                       ic.ic_number COLLATE utf8mb4_unicode_ci

            LEFT JOIN (
                SELECT icr1.*
                FROM ibs_call_registration icr1
                INNER JOIN (
                    SELECT
                        call_number,
                        MAX(version) AS max_version
                    FROM ibs_call_registration
                    GROUP BY call_number
                ) latest
                    ON latest.call_number = icr1.call_number
                   AND latest.max_version = icr1.version
            ) icr
                    ON icr.call_number COLLATE utf8mb4_unicode_ci =
                       ic.ic_number COLLATE utf8mb4_unicode_ci

            LEFT JOIN sarthi_ibs_poi_mapping pm
                   ON pm.poi_code = ic.place_of_inspection
                  AND pm.product_type = 'erc'

            WHERE icr.call_number IS NULL
               OR UPPER(icr.status) = 'FAILED'

            GROUP BY
                ph.case_no,
                ic.created_at,
                ic.place_of_inspection,
                pm.ibs_vendor_code,
                um.employee_code,
                ic.po_serial_no,
                rm.book_no,
                rm.set_no,
                rm.created_at,
                ic.ic_number,
                rm.ic_number

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
}