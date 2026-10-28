package com.sarthi.repository.processmaterial;

import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessInspectionDetailsRepository extends JpaRepository<ProcessInspectionDetails, Long> {

    /**
     * Find Process Inspection Details by Inspection Call ID
     * Returns a list to support multiple lots for the same inspection call
     */
    @Query("SELECT pd FROM ProcessInspectionDetails pd WHERE pd.inspectionCall.id = :icId")
    List<ProcessInspectionDetails> findByIcId(@Param("icId") Long icId);
    
    List<ProcessInspectionDetails> findByInspectionCallIdIn(List<Long> icIds);

    /**
     * Find Process Inspection Details by RM IC Number
     */
    Optional<ProcessInspectionDetails> findByRmIcNumber(String rmIcNumber);

    /**
     * Find Process Inspection Details by Lot Number
     */
    Optional<ProcessInspectionDetails> findByLotNumber(String lotNumber);



    @Query("SELECT COALESCE(SUM(pd.offeredQty),0) " +
            "FROM ProcessInspectionDetails pd " +
            "WHERE pd.inspectionCall.id = :icId")
    int sumOfferedQtyByIcId(@Param("icId") Long icId);


//    @Query("""
//SELECT COALESCE(pd.offeredQty, 0)
//FROM ProcessInspectionDetails pd
//WHERE pd.inspectionCall.id = :icId
//""")
//    Integer findOfferedQtyByIcId(@Param("icId") Long icId);
@Query("""
    SELECT COALESCE(SUM(pd.offeredQty), 0)
    FROM ProcessInspectionDetails pd
    WHERE pd.inspectionCall.id = :icId
""")
Integer findOfferedQtyByIcId(@Param("icId") Long icId);

    /**
     * Calculate total offered quantity for a specific heat number across multiple inspection calls
     * Used to calculate "Offered Earlier" for heat summary
     */
    @Query("""
    SELECT COALESCE(SUM(pd.offeredQty), 0)
    FROM ProcessInspectionDetails pd
    WHERE pd.inspectionCall.icNumber IN :callNos
    AND pd.heatNumber = :heatNo
""")
    Integer sumOfferedQtyByCallNosAndHeatNo(
            @Param("callNos") List<String> callNos,
            @Param("heatNo") String heatNo
    );

    /**
     * Calculate total offered quantity for a specific heat number scoped strictly to an RM IC
     */
    @Query(value = """
        SELECT COALESCE(SUM(pid.offered_qty), 0)
        FROM process_inspection_details pid
        WHERE pid.heat_number = :heatNo
        AND (
            (:rmIcId IS NOT NULL AND pid.rm_ic_id = :rmIcId)
            OR pid.rm_ic_number IN :rmCallCandidates
            OR (:callPattern IS NOT NULL AND :callPattern != '' AND pid.rm_ic_number LIKE CONCAT('%', :callPattern, '%'))
            OR EXISTS (
                SELECT 1 FROM process_rm_ic_mapping prim
                WHERE prim.process_ic_id = pid.ic_id
                AND (
                    (:rmIcId IS NOT NULL AND prim.rm_ic_id = :rmIcId)
                    OR prim.rm_ic_number IN :rmCallCandidates
                    OR (:callPattern IS NOT NULL AND :callPattern != '' AND prim.rm_ic_number LIKE CONCAT('%', :callPattern, '%'))
                )
            )
        )
    """, nativeQuery = true)
    Integer sumOfferedQtyByRmIcAndHeatNo(
            @Param("rmIcId") Long rmIcId,
            @Param("rmCallCandidates") List<String> rmCallCandidates,
            @Param("callPattern") String callPattern,
            @Param("heatNo") String heatNo
    );

    /**
     * Find distinct process call numbers raised under a specific RM IC
     */
    @Query(value = """
        SELECT DISTINCT ic.ic_number
        FROM inspection_calls ic
        WHERE EXISTS (
            SELECT 1 FROM process_inspection_details pid
            WHERE pid.ic_id = ic.id
            AND (
                (:rmIcId IS NOT NULL AND pid.rm_ic_id = :rmIcId)
                OR pid.rm_ic_number IN :rmCallCandidates
                OR (:callPattern IS NOT NULL AND :callPattern != '' AND pid.rm_ic_number LIKE CONCAT('%', :callPattern, '%'))
            )
        )
        OR EXISTS (
            SELECT 1 FROM process_rm_ic_mapping prim
            WHERE prim.process_ic_id = ic.id
            AND (
                (:rmIcId IS NOT NULL AND prim.rm_ic_id = :rmIcId)
                OR prim.rm_ic_number IN :rmCallCandidates
                OR (:callPattern IS NOT NULL AND :callPattern != '' AND prim.rm_ic_number LIKE CONCAT('%', :callPattern, '%'))
            )
        )
    """, nativeQuery = true)
    List<String> findProcessCallNumbersByRmIc(
            @Param("rmIcId") Long rmIcId,
            @Param("rmCallCandidates") List<String> rmCallCandidates,
            @Param("callPattern") String callPattern
    );

    /**
     * Find distinct RM IC numbers by Process IC certificate number
     * Joins with inspection_calls and inspection_complete_details
     */
    @Query(value = "SELECT DISTINCT pid.rm_ic_number " +
            "FROM process_inspection_details pid " +
            "INNER JOIN inspection_calls ic ON pid.ic_id = ic.id " +
            "INNER JOIN inspection_complete_details icd ON ic.ic_number = icd.CALL_NO " +
            "WHERE icd.CERTIFICATE_NO = :certificateNo " +
            "AND pid.rm_ic_number IS NOT NULL " +
            "ORDER BY pid.rm_ic_number",
            nativeQuery = true)
    List<String> findRmIcNumbersByCertificateNo(@Param("certificateNo") String certificateNo);

    /**
     * Find distinct lot numbers by RM IC number
     */
    @Query(value = "SELECT DISTINCT lot_number " +
            "FROM process_inspection_details " +
            "WHERE rm_ic_number = :rmIcNumber " +
            "AND lot_number IS NOT NULL " +
            "ORDER BY lot_number",
            nativeQuery = true)
    List<String> findLotNumbersByRmIcNumber(@Param("rmIcNumber") String rmIcNumber);

    /**
     * Find lot numbers by RM IC certificate and Process IC certificate
     * rm_ic_number in process_inspection_details / process_rm_ic_mapping stores CERTIFICATE_NO directly
     */
    @Query(value = "SELECT DISTINCT pid.lot_number " +
            "FROM process_inspection_details pid " +
            "INNER JOIN inspection_calls ic ON pid.ic_id = ic.id " +
            "INNER JOIN inspection_complete_details icd_process ON ic.ic_number = icd_process.CALL_NO " +
            "LEFT JOIN process_rm_ic_mapping prim ON prim.process_ic_id = ic.id " +
            "WHERE (TRIM(pid.rm_ic_number) = :rmCertificateNo OR TRIM(prim.rm_ic_number) = :rmCertificateNo) " +
            "AND icd_process.CERTIFICATE_NO = :processCertificateNo " +
            "AND pid.lot_number IS NOT NULL " +
            "ORDER BY pid.lot_number",
            nativeQuery = true)
    List<String> findLotNumbersByRmAndProcessIcNumbers(
            @Param("rmCertificateNo") String rmCertificateNo,
            @Param("processCertificateNo") String processCertificateNo);

    /**
     * Find heat numbers by lot number and RM IC certificate
     * Returns heat numbers for a specific lot that matches the RM IC certificate
     */
    @Query(value = "SELECT heat_no FROM (" +
            "  SELECT pid.heat_number AS heat_no, pid.ic_id AS sort_id " +
            "  FROM process_inspection_details pid " +
            "  LEFT JOIN inspection_calls ic ON pid.ic_id = ic.id " +
            "  LEFT JOIN inspection_complete_details icd ON ic.ic_number COLLATE utf8mb4_unicode_ci = icd.CALL_NO COLLATE utf8mb4_unicode_ci " +
            "  LEFT JOIN PROCESS_IC_EDIT pie ON pie.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "  LEFT JOIN process_rm_ic_mapping prim ON prim.process_ic_id = pid.ic_id " +
            "  WHERE TRIM(pid.lot_number) = TRIM(:lotNumber) " +
            "    AND (:rmCertificateNo IS NULL OR :rmCertificateNo = '' OR TRIM(pid.rm_ic_number) = TRIM(:rmCertificateNo) OR TRIM(prim.rm_ic_number) = TRIM(:rmCertificateNo)) " +
            "    AND (:processCertificateNo IS NULL OR :processCertificateNo = '' " +
            "         OR TRIM(ic.ic_number) = TRIM(:processCertificateNo) " +
            "         OR TRIM(icd.CERTIFICATE_NO) = TRIM(:processCertificateNo) " +
            "         OR TRIM(pie.IC_NUMBER) = TRIM(:processCertificateNo) " +
            "         OR :processCertificateNo LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%')) " +
            "    AND pid.heat_number IS NOT NULL AND pid.heat_number <> '' " +
            "  UNION " +
            "  SELECT item.heat_no AS heat_no, 0 AS sort_id " +
            "  FROM erc_material_diversion_item item " +
            "  INNER JOIN erc_material_diversion_request req ON item.diversion_request_id = req.id " +
            "  LEFT JOIN inspection_calls ic_div ON (ic_div.diversion_request_no COLLATE utf8mb4_unicode_ci = req.request_no COLLATE utf8mb4_unicode_ci OR ic_div.ic_number COLLATE utf8mb4_unicode_ci = req.request_no COLLATE utf8mb4_unicode_ci) " +
            "  LEFT JOIN inspection_complete_details icd_div ON (icd_div.CALL_NO COLLATE utf8mb4_unicode_ci = req.request_no COLLATE utf8mb4_unicode_ci OR (ic_div.ic_number IS NOT NULL AND icd_div.CALL_NO COLLATE utf8mb4_unicode_ci = ic_div.ic_number COLLATE utf8mb4_unicode_ci)) " +
            "  LEFT JOIN PROCESS_IC_EDIT pie_div ON (pie_div.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', req.request_no COLLATE utf8mb4_unicode_ci, '%') OR (ic_div.ic_number IS NOT NULL AND pie_div.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic_div.ic_number COLLATE utf8mb4_unicode_ci, '%'))) " +
            "  WHERE TRIM(item.lot_no) = TRIM(:lotNumber) " +
            "    AND (:processCertificateNo IS NULL OR :processCertificateNo = '' " +
            "         OR TRIM(req.request_no) = TRIM(:processCertificateNo) " +
            "         OR TRIM(req.diversion_no) = TRIM(:processCertificateNo) " +
            "         OR (ic_div.ic_number IS NOT NULL AND TRIM(ic_div.ic_number) = TRIM(:processCertificateNo)) " +
            "         OR (icd_div.CERTIFICATE_NO IS NOT NULL AND TRIM(icd_div.CERTIFICATE_NO) = TRIM(:processCertificateNo)) " +
            "         OR (pie_div.IC_NUMBER IS NOT NULL AND TRIM(pie_div.IC_NUMBER) = TRIM(:processCertificateNo)) " +
            "         OR (ic_div.ic_number IS NOT NULL AND :processCertificateNo LIKE CONCAT('%', ic_div.ic_number COLLATE utf8mb4_unicode_ci, '%')) " +
            "         OR :processCertificateNo LIKE CONCAT('%', req.request_no COLLATE utf8mb4_unicode_ci, '%')) " +
            "    AND item.heat_no IS NOT NULL AND item.heat_no <> '' " +
            "  UNION " +
            "  SELECT pid_src.heat_number AS heat_no, pid_src.ic_id AS sort_id " +
            "  FROM inspection_calls ic_div " +
            "  JOIN inspection_calls ic_src ON (ic_src.ic_number COLLATE utf8mb4_unicode_ci = ic_div.source_ic_no COLLATE utf8mb4_unicode_ci OR ic_div.source_ic_no COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic_src.ic_number COLLATE utf8mb4_unicode_ci, '%')) " +
            "  JOIN process_inspection_details pid_src ON pid_src.ic_id = ic_src.id " +
            "  LEFT JOIN inspection_complete_details icd_div ON icd_div.CALL_NO COLLATE utf8mb4_unicode_ci = ic_div.ic_number COLLATE utf8mb4_unicode_ci " +
            "  LEFT JOIN PROCESS_IC_EDIT pie_div ON pie_div.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic_div.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "  WHERE TRIM(pid_src.lot_number) = TRIM(:lotNumber) " +
            "    AND (ic_div.is_diverted_material = 1 OR ic_div.call_category = 'DIVERTED_PASSED' OR ic_div.call_category = 'DIVERTED') " +
            "    AND (:processCertificateNo IS NULL OR :processCertificateNo = '' " +
            "         OR TRIM(ic_div.ic_number) = TRIM(:processCertificateNo) " +
            "         OR (icd_div.CERTIFICATE_NO IS NOT NULL AND TRIM(icd_div.CERTIFICATE_NO) = TRIM(:processCertificateNo)) " +
            "         OR (pie_div.IC_NUMBER IS NOT NULL AND TRIM(pie_div.IC_NUMBER) = TRIM(:processCertificateNo)) " +
            "         OR :processCertificateNo LIKE CONCAT('%', ic_div.ic_number COLLATE utf8mb4_unicode_ci, '%')) " +
            "    AND pid_src.heat_number IS NOT NULL AND pid_src.heat_number <> '' " +
            ") heat_tbl " +
            "ORDER BY sort_id DESC",
            nativeQuery = true)
    List<String> findHeatNumbersByLotNumber(
            @Param("lotNumber") String lotNumber,
            @Param("rmCertificateNo") String rmCertificateNo,
            @Param("processCertificateNo") String processCertificateNo);

    /**
     * Find lot numbers by multiple RM IC certificates and multiple Process IC certificates
     * Returns all unique lot numbers that match any combination of the specified certificates
     */
    @Query(value = "SELECT DISTINCT lot_num FROM (" +
            "  SELECT DISTINCT pid.lot_number AS lot_num " +
            "  FROM process_inspection_details pid " +
            "  INNER JOIN inspection_calls ic ON pid.ic_id = ic.id " +
            "  LEFT JOIN inspection_complete_details icd_process ON ic.ic_number COLLATE utf8mb4_unicode_ci = icd_process.CALL_NO COLLATE utf8mb4_unicode_ci " +
            "  LEFT JOIN PROCESS_IC_EDIT pie ON pie.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "  LEFT JOIN process_rm_ic_mapping prim ON prim.process_ic_id = ic.id " +
            "  WHERE ('ALL' IN :rmCertificateNos OR TRIM(pid.rm_ic_number) IN :rmCertificateNos OR TRIM(prim.rm_ic_number) IN :rmCertificateNos) " +
            "    AND (icd_process.CERTIFICATE_NO IN :processCertificateNos OR ic.ic_number IN :processCertificateNos OR pie.IC_NUMBER IN :processCertificateNos) " +
            "    AND pid.lot_number IS NOT NULL " +
            "  UNION " +
            "  SELECT DISTINCT item.lot_no AS lot_num " +
            "  FROM erc_material_diversion_item item " +
            "  INNER JOIN erc_material_diversion_request req ON item.diversion_request_id = req.id " +
            "  LEFT JOIN inspection_calls ic_div ON (ic_div.diversion_request_no COLLATE utf8mb4_unicode_ci = req.request_no COLLATE utf8mb4_unicode_ci OR ic_div.ic_number COLLATE utf8mb4_unicode_ci = req.request_no COLLATE utf8mb4_unicode_ci) " +
            "  LEFT JOIN inspection_complete_details icd_div ON (icd_div.CALL_NO COLLATE utf8mb4_unicode_ci = req.request_no COLLATE utf8mb4_unicode_ci OR (ic_div.ic_number IS NOT NULL AND icd_div.CALL_NO COLLATE utf8mb4_unicode_ci = ic_div.ic_number COLLATE utf8mb4_unicode_ci)) " +
            "  LEFT JOIN PROCESS_IC_EDIT pie_div ON (pie_div.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', req.request_no COLLATE utf8mb4_unicode_ci, '%') OR (ic_div.ic_number IS NOT NULL AND pie_div.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic_div.ic_number COLLATE utf8mb4_unicode_ci, '%'))) " +
            "  WHERE (req.request_no IN :processCertificateNos OR req.diversion_no IN :processCertificateNos " +
            "     OR (ic_div.ic_number IS NOT NULL AND ic_div.ic_number IN :processCertificateNos) " +
            "     OR (icd_div.CERTIFICATE_NO IS NOT NULL AND icd_div.CERTIFICATE_NO IN :processCertificateNos) " +
            "     OR (pie_div.IC_NUMBER IS NOT NULL AND pie_div.IC_NUMBER IN :processCertificateNos)) " +
            "    AND item.lot_no IS NOT NULL " +
            "  UNION " +
            "  SELECT DISTINCT pid_src.lot_number AS lot_num " +
            "  FROM inspection_calls ic_div " +
            "  JOIN inspection_calls ic_src ON (ic_src.ic_number COLLATE utf8mb4_unicode_ci = ic_div.source_ic_no COLLATE utf8mb4_unicode_ci OR ic_div.source_ic_no COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic_src.ic_number COLLATE utf8mb4_unicode_ci, '%')) " +
            "  JOIN process_inspection_details pid_src ON pid_src.ic_id = ic_src.id " +
            "  LEFT JOIN inspection_complete_details icd_div ON icd_div.CALL_NO COLLATE utf8mb4_unicode_ci = ic_div.ic_number COLLATE utf8mb4_unicode_ci " +
            "  LEFT JOIN PROCESS_IC_EDIT pie_div ON pie_div.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic_div.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "  WHERE (ic_div.is_diverted_material = 1 OR ic_div.call_category = 'DIVERTED_PASSED' OR ic_div.call_category = 'DIVERTED') " +
            "    AND (ic_div.ic_number IN :processCertificateNos OR icd_div.CERTIFICATE_NO IN :processCertificateNos OR pie_div.IC_NUMBER IN :processCertificateNos) " +
            "    AND pid_src.lot_number IS NOT NULL " +
            ") lot_tbl " +
            "ORDER BY lot_num",
            nativeQuery = true)
    List<String> findLotNumbersByMultipleRmAndProcessIcNumbers(
            @Param("rmCertificateNos") List<String> rmCertificateNos,
            @Param("processCertificateNos") List<String> processCertificateNos);


}

