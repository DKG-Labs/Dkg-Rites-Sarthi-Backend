package com.sarthi.repository;


import com.sarthi.entity.InspectionCompleteDetails;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionCompleteDetailsRepository extends JpaRepository<InspectionCompleteDetails, Long> {

    /**
     * Find InspectionCompleteDetails by call number (Safely ordered by ID desc to prevent duplicate result errors)
     */
    @Query(value = "SELECT * FROM inspection_complete_details WHERE CALL_NO = :callNo ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<InspectionCompleteDetails> findByCallNo(@Param("callNo") String callNo);

    /**
     * Find the most recent InspectionCompleteDetails by call number (in case of duplicates)
     */
    Optional<InspectionCompleteDetails> findFirstByCallNoOrderByCreatedOnDesc(String callNo);

    /**
     * Find InspectionCompleteDetails by certificate number
     * Used to map certificate number to IC number for Process IC
     */
    @Query(value = "SELECT * FROM inspection_complete_details WHERE CERTIFICATE_NO = :certificateNo ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<InspectionCompleteDetails> findByCertificateNo(@Param("certificateNo") String certificateNo);

    /**
     * Find all certificate numbers for Process ICs (EP prefix) filtered by vendor
     * Joins with inspection_calls table to filter by vendor_id
     */
    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "WHERE icd.CALL_NO LIKE 'EP%' " +
            "AND ic.vendor_id = :vendorId " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<String> findProcessIcCertificateNumbersByVendor(@Param("vendorId") String vendorId);

    /**
     * Find all certificate numbers for RM ICs (ER prefix) filtered by PO number
     * Used for Process Inspection Call dropdown to show only RM ICs for the specific PO
     */
    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO " +
            "FROM inspection_complete_details icd " +
            "WHERE icd.CALL_NO LIKE 'ER-%' " +
            "AND icd.PO_NO = :poNo " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<String> findCompletedRmIcCertificateNumbersByPoNo(@Param("poNo") String poNo);

    /**
     * Find all certificate numbers for RM ICs (ER prefix) filtered by PO Serial Number
     * Joins inspection_calls table to filter by po_serial_no
     * Used for Process Inspection Call dropdown to show only RM ICs for the specific PO Serial Number
     */
    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "WHERE icd.CALL_NO LIKE 'ER-%' " +
            "AND ic.po_serial_no = :poSerialNo " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<String> findCompletedRmIcCertificateNumbersByPoSerialNo(@Param("poSerialNo") String poSerialNo);

    /**
     * Find all RM IC certificate numbers for Final Inspection Call dropdown
     * Returns CERTIFICATE_NO for display in dropdown
     */
    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "WHERE icd.CALL_NO LIKE 'ER-%' " +
            "AND ic.vendor_id = :vendorId " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<String> findRmIcNumbersByVendor(@Param("vendorId") String vendorId);

    /**
     * Find Process IC certificate numbers by RM IC certificate number
     * Returns CERTIFICATE_NO for display in dropdown
     * Logic: rm_ic_number in process_inspection_details / process_rm_ic_mapping stores CERTIFICATE_NO directly
     */
    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "WHERE icd.CALL_NO IN (" +
            "    SELECT DISTINCT ic2.ic_number " +
            "    FROM inspection_calls ic2 " +
            "    LEFT JOIN process_inspection_details pid ON pid.ic_id = ic2.id " +
            "    LEFT JOIN process_rm_ic_mapping prim ON prim.process_ic_id = ic2.id " +
            "    WHERE (TRIM(pid.rm_ic_number) = :rmCertificateNo OR TRIM(prim.rm_ic_number) = :rmCertificateNo) " +
            ") " +
            "AND (ic.type_of_call LIKE '%PROCESS%' OR icd.CALL_NO LIKE 'EP%' OR icd.CALL_NO LIKE 'ER%') " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<String> findProcessIcNumbersByRmIcNumber(@Param("rmCertificateNo") String rmCertificateNo);

    /**
     * Find Process IC certificate numbers for multiple RM IC certificates
     * Returns all unique Process IC certificates that used any of the specified RM ICs
     */
    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "WHERE icd.CALL_NO IN (" +
            "    SELECT DISTINCT ic2.ic_number " +
            "    FROM inspection_calls ic2 " +
            "    LEFT JOIN process_inspection_details pid ON pid.ic_id = ic2.id " +
            "    LEFT JOIN process_rm_ic_mapping prim ON prim.process_ic_id = ic2.id " +
            "    WHERE (TRIM(pid.rm_ic_number) IN :rmCertificateNos OR TRIM(prim.rm_ic_number) IN :rmCertificateNos) " +
            ") " +
            "AND (ic.type_of_call LIKE '%PROCESS%' OR icd.CALL_NO LIKE 'EP%' OR icd.CALL_NO LIKE 'ER%') " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<String> findProcessIcNumbersByMultipleRmIcNumbers(@Param("rmCertificateNos") List<String> rmCertificateNos);

    @Query(value = "SELECT CERTIFICATE_NO FROM inspection_complete_details WHERE CALL_NO = :callNo ORDER BY id DESC LIMIT 1", nativeQuery = true)
    String findCertificateNoByCallNo(@Param("callNo") String callNo);

  @Query("""
    SELECT i.callNo, i.certificateNo
    FROM InspectionCompleteDetails i
    WHERE i.callNo IN :callNos
""")
List<Object[]> findCertificateNosByCallNos(
        @Param("callNos") List<String> callNos
);

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(rm.created_at, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "LEFT JOIN rm_ic_edit rm ON icd.CERTIFICATE_NO = rm.ic_number COLLATE utf8mb4_unicode_ci " +
            "WHERE icd.CALL_NO LIKE 'ER-%' " +
            "AND ic.po_serial_no = :poSerialNo " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findCompletedRmIcCertificateNumbersWithDateByPoSerialNo(@Param("poSerialNo") String poSerialNo);

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(rm.created_at, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "LEFT JOIN rm_ic_edit rm ON icd.CERTIFICATE_NO = rm.ic_number COLLATE utf8mb4_unicode_ci " +
            "WHERE icd.CALL_NO LIKE 'ER-%' " +
            "AND ic.po_serial_no = :poSerialNo " +
            "AND (ic.is_diverted_material IS NULL OR ic.is_diverted_material = 0) " +
            "AND (ic.call_category IS NULL OR (ic.call_category != 'DIVERTED_PASSED' AND ic.call_category != 'DIVERTED')) " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findFreshCompletedRmIcCertificateNumbersWithDateByPoSerialNo(@Param("poSerialNo") String poSerialNo);

    @Query(value = "SELECT cert.certificateNo, MAX(cert.createdAt) AS createdAt FROM (" +
            "  SELECT DISTINCT rm.ic_number AS certificateNo, COALESCE(rm.created_at, icd.CREATED_ON, ic.created_at) AS createdAt " +
            "  FROM inspection_calls ic " +
            "  LEFT JOIN inspection_complete_details icd ON icd.CALL_NO COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci " +
            "  JOIN rm_ic_edit rm ON (" +
            "      rm.ic_number COLLATE utf8mb4_unicode_ci = icd.CERTIFICATE_NO COLLATE utf8mb4_unicode_ci " +
            "      OR rm.ic_number COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci " +
            "      OR rm.ic_number COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "      OR ic.ic_number COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(SUBSTRING_INDEX(rm.ic_number, '/', 2), '/', -1) COLLATE utf8mb4_unicode_ci " +
            "  ) " +
            "  WHERE ic.po_serial_no = :poSerialNo " +
            "    AND UPPER(TRIM(ic.status)) = 'SEND_CALL_TO_IBS' " +
            "    AND (ic.is_diverted_material = 1 OR ic.call_category = 'DIVERTED_PASSED' OR ic.call_category = 'DIVERTED') " +
            "    AND (ic.type_of_call LIKE '%RAW%' OR ic.ic_number LIKE 'ER%') " +
            "    AND rm.ic_number IS NOT NULL AND TRIM(rm.ic_number) != '' " +
            ") cert WHERE cert.certificateNo IS NOT NULL " +
            "GROUP BY cert.certificateNo " +
            "ORDER BY cert.certificateNo DESC",
            nativeQuery = true)
    List<Object[]> findDivertedRmIcCertificateNumbersWithDateByPoSerialNo(@Param("poSerialNo") String poSerialNo);

    @Query(value = "SELECT cert.certificateNo, MAX(cert.createdAt) AS createdAt FROM (" +
            "  SELECT DISTINCT p.IC_NUMBER AS certificateNo, COALESCE(p.CREATED_AT, icd.CREATED_ON, ic.created_at) AS createdAt " +
            "  FROM inspection_calls ic " +
            "  LEFT JOIN inspection_complete_details icd ON icd.CALL_NO COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci " +
            "  JOIN PROCESS_IC_EDIT p ON (" +
            "      p.IC_NUMBER COLLATE utf8mb4_unicode_ci = icd.CERTIFICATE_NO COLLATE utf8mb4_unicode_ci " +
            "      OR p.IC_NUMBER COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci " +
            "      OR p.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "      OR ic.ic_number COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(SUBSTRING_INDEX(p.IC_NUMBER, '/', 2), '/', -1) COLLATE utf8mb4_unicode_ci " +
            "  ) " +
            "  WHERE ic.po_serial_no = :poSerialNo " +
            "    AND UPPER(TRIM(ic.status)) = 'SEND_CALL_TO_IBS' " +
            "    AND (ic.is_diverted_material = 1 OR ic.call_category = 'DIVERTED_PASSED' OR ic.call_category = 'DIVERTED') " +
            "    AND (ic.type_of_call LIKE '%PROCESS%' OR ic.ic_number LIKE 'EP%') " +
            "    AND p.IC_NUMBER IS NOT NULL AND TRIM(p.IC_NUMBER) != '' " +
            ") cert WHERE cert.certificateNo IS NOT NULL " +
            "GROUP BY cert.certificateNo " +
            "ORDER BY cert.certificateNo DESC",
            nativeQuery = true)
    List<Object[]> findDivertedProcessIcCertificateNumbersWithDateByPoSerialNo(@Param("poSerialNo") String poSerialNo);

    @Query(value = "SELECT DISTINCT cert.rm_cert FROM (" +
            "  SELECT DISTINCT COALESCE(prim.rm_ic_number, pid.rm_ic_number) AS rm_cert " +
            "  FROM inspection_complete_details icd " +
            "  INNER JOIN inspection_calls ic ON icd.CALL_NO COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci " +
            "  LEFT JOIN process_inspection_details pid ON pid.ic_id = ic.id " +
            "  LEFT JOIN process_rm_ic_mapping prim ON prim.process_ic_id = ic.id " +
            "  WHERE icd.CERTIFICATE_NO = :processCertNo " +
            "     OR ic.ic_number = :processCertNo " +
            "     OR :processCertNo LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "     OR ic.ic_number COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(SUBSTRING_INDEX(:processCertNo, '/', 2), '/', -1) " +
            "  UNION " +
            "  SELECT DISTINCT COALESCE(prim2.rm_ic_number, pid2.rm_ic_number) AS rm_cert " +
            "  FROM erc_material_diversion_request req " +
            "  LEFT JOIN inspection_complete_details icd_src ON (icd_src.CALL_NO = req.source_call_no OR icd_src.CERTIFICATE_NO = req.source_ic_no) " +
            "  LEFT JOIN inspection_calls ic_src ON (ic_src.ic_number = icd_src.CALL_NO OR ic_src.ic_number = req.source_call_no) " +
            "  LEFT JOIN process_inspection_details pid2 ON pid2.ic_id = ic_src.id " +
            "  LEFT JOIN process_rm_ic_mapping prim2 ON prim2.process_ic_id = ic_src.id " +
            "  WHERE req.request_no = :processCertNo OR (SELECT icd_div.CERTIFICATE_NO FROM inspection_complete_details icd_div WHERE icd_div.CALL_NO = req.request_no LIMIT 1) = :processCertNo " +
            "  UNION " +
            "  SELECT DISTINCT COALESCE(prim3.rm_ic_number, pid3.rm_ic_number) AS rm_cert " +
            "  FROM inspection_calls ic_div " +
            "  JOIN inspection_calls ic_src ON (ic_src.ic_number = ic_div.source_ic_no) " +
            "  LEFT JOIN process_inspection_details pid3 ON pid3.ic_id = ic_src.id " +
            "  LEFT JOIN process_rm_ic_mapping prim3 ON prim3.process_ic_id = ic_src.id " +
            "  WHERE ic_div.ic_number = :processCertNo " +
            ") cert WHERE cert.rm_cert IS NOT NULL AND cert.rm_cert != ''",
            nativeQuery = true)
    List<String> findRmIcsByProcessCertificateNo(@Param("processCertNo") String processCertNo);

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(rm.created_at, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "LEFT JOIN rm_ic_edit rm ON icd.CERTIFICATE_NO = rm.ic_number COLLATE utf8mb4_unicode_ci " +
            "WHERE icd.CALL_NO LIKE 'ER-%' " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findAllCompletedRmIcsWithDate();

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(p.CREATED_AT, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "LEFT JOIN PROCESS_IC_EDIT p ON icd.CERTIFICATE_NO = p.IC_NUMBER COLLATE utf8mb4_unicode_ci " +
            "WHERE (ic.type_of_call LIKE '%PROCESS%' OR icd.CALL_NO LIKE 'EP%' OR icd.CALL_NO LIKE 'ER%') " +
            "AND ic.vendor_id = :vendorId " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findProcessIcCertificateNumbersWithDateByVendor(@Param("vendorId") String vendorId);

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(p.CREATED_AT, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "LEFT JOIN PROCESS_IC_EDIT p ON icd.CERTIFICATE_NO = p.IC_NUMBER COLLATE utf8mb4_unicode_ci " +
            "WHERE icd.CALL_NO IN (" +
            "    SELECT DISTINCT ic2.ic_number " +
            "    FROM inspection_calls ic2 " +
            "    LEFT JOIN process_inspection_details pid ON pid.ic_id = ic2.id " +
            "    LEFT JOIN process_rm_ic_mapping prim ON prim.process_ic_id = ic2.id " +
            "    WHERE (TRIM(pid.rm_ic_number) = :rmCertificateNo OR TRIM(prim.rm_ic_number) = :rmCertificateNo) " +
            ") " +
            "AND (ic.type_of_call LIKE '%PROCESS%' OR icd.CALL_NO LIKE 'EP%' OR icd.CALL_NO LIKE 'ER%') " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findProcessIcNumbersWithDateByRmIcNumber(@Param("rmCertificateNo") String rmCertificateNo);

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(p.CREATED_AT, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "LEFT JOIN PROCESS_IC_EDIT p ON icd.CERTIFICATE_NO = p.IC_NUMBER COLLATE utf8mb4_unicode_ci " +
            "WHERE icd.CALL_NO IN (" +
            "    SELECT DISTINCT ic2.ic_number " +
            "    FROM inspection_calls ic2 " +
            "    LEFT JOIN process_inspection_details pid ON pid.ic_id = ic2.id " +
            "    LEFT JOIN process_rm_ic_mapping prim ON prim.process_ic_id = ic2.id " +
            "    WHERE (TRIM(pid.rm_ic_number) IN :rmCertificateNos OR TRIM(prim.rm_ic_number) IN :rmCertificateNos) " +
            ") " +
            "AND (ic.type_of_call LIKE '%PROCESS%' OR icd.CALL_NO LIKE 'EP%' OR icd.CALL_NO LIKE 'ER%') " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findProcessIcNumbersWithDateByMultipleRmIcNumbers(@Param("rmCertificateNos") List<String> rmCertificateNos);

    @Query(value = "SELECT " +
            "COALESCE(NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(rie.ic_number, '/', 2), '/', -1), ''), rie.ic_number) AS certificate_no, " +
            "COALESCE(NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(rie.ic_number, '/', 2), '/', -1), ''), rie.ic_number) AS call_no, " +
            "ic.po_no, " +
            "ic.po_serial_no, " +
            "ic.actual_inspection_date, " +
            "COALESCE((SELECT SUM(fr.weight_accepted_mt) FROM rm_heat_final_result fr WHERE fr.inspection_call_no COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci), " +
            "         (SELECT SUM(hq.qty_accepted) FROM rm_heat_quantities hq JOIN rm_inspection_details rmd ON hq.rm_detail_id = rmd.id WHERE rmd.ic_id = ic.id), 0.0) AS total_accepted, " +
            "MAX(um.FULL_NAME) AS ie_name, " +
            "MAX(cs.BLOB_URL) AS cert_doc_url, " +
            "MAX(rie.created_by) AS ie_user_id " +
            "FROM rm_ic_edit rie " +
            "INNER JOIN inspection_calls ic ON (ic.ic_number COLLATE utf8mb4_unicode_ci = rie.ic_number COLLATE utf8mb4_unicode_ci OR ic.ic_number COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(SUBSTRING_INDEX(rie.ic_number, '/', 2), '/', -1) COLLATE utf8mb4_unicode_ci) " +
            "LEFT JOIN user_master um ON CAST(um.USERID AS CHAR) COLLATE utf8mb4_unicode_ci = rie.created_by COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN certificate_storage cs ON (" +
            "    cs.IC_NUMBER COLLATE utf8mb4_unicode_ci = rie.ic_number COLLATE utf8mb4_unicode_ci " +
            "    OR cs.IC_NUMBER COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci " +
            "    OR cs.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "    OR cs.FILE_NAME COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            ") " +
            "WHERE (rie.ic_number LIKE '%/ER-%/%' OR rie.ic_number LIKE 'ER-%' OR rie.ic_number LIKE '%/ER-%') " +
            "AND (:vendorCode IS NULL OR ic.vendor_id = :vendorCode OR TRIM(LEADING ':' FROM ic.vendor_id) = TRIM(LEADING ':' FROM :vendorCode)) " +
            "AND (:plantParam IS NULL OR CAST(ic.unit_id AS CHAR) = :plantParam OR ic.unit_name = :plantParam OR ic.place_of_inspection = :plantParam) " +
            "GROUP BY rie.ic_number, ic.id, ic.po_no, ic.po_serial_no, ic.actual_inspection_date " +
            "ORDER BY rie.ic_number DESC",
            nativeQuery = true)
    List<Object[]> findEligibleRmSourceIcs(
            @Param("vendorCode") String vendorCode,
            @Param("plantParam") String plantParam
    );

    @Query(value = "SELECT " +
            "COALESCE(NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(pie.IC_NUMBER, '/', 2), '/', -1), ''), pie.IC_NUMBER) AS certificate_no, " +
            "COALESCE(NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(pie.IC_NUMBER, '/', 2), '/', -1), ''), pie.IC_NUMBER) AS call_no, " +
            "ic.po_no, " +
            "ic.po_serial_no, " +
            "ic.actual_inspection_date, " +
            "COALESCE((SELECT GREATEST(0, COALESCE(SUM(pfr.shearing_manufactured), 0) - COALESCE(SUM(pfr.total_rejected), 0)) FROM process_line_final_result pfr WHERE pfr.inspection_call_no COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci), " +
            "         (SELECT SUM(pfr2.total_accepted) FROM process_line_final_result pfr2 WHERE pfr2.inspection_call_no COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci), " +
            "         (SELECT SUM(pid.qty_accepted) FROM process_inspection_details pid WHERE pid.ic_id = ic.id), " +
            "         (SELECT SUM(pid2.offered_qty) FROM process_inspection_details pid2 WHERE pid2.ic_id = ic.id), 0.0) AS total_accepted, " +
            "MAX(um.FULL_NAME) AS ie_name, " +
            "MAX(cs.BLOB_URL) AS cert_doc_url, " +
            "MAX(pie.CREATED_BY) AS ie_user_id " +
            "FROM PROCESS_IC_EDIT pie " +
            "INNER JOIN inspection_calls ic ON (ic.ic_number COLLATE utf8mb4_unicode_ci = pie.IC_NUMBER COLLATE utf8mb4_unicode_ci OR ic.ic_number COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(SUBSTRING_INDEX(pie.IC_NUMBER, '/', 2), '/', -1) COLLATE utf8mb4_unicode_ci) " +
            "LEFT JOIN user_master um ON CAST(um.USERID AS CHAR) COLLATE utf8mb4_unicode_ci = pie.CREATED_BY COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN certificate_storage cs ON (" +
            "    cs.IC_NUMBER COLLATE utf8mb4_unicode_ci = pie.IC_NUMBER COLLATE utf8mb4_unicode_ci " +
            "    OR cs.IC_NUMBER COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci " +
            "    OR cs.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "    OR cs.FILE_NAME COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            ") " +
            "WHERE (pie.IC_NUMBER LIKE '%/EP-%/%' OR pie.IC_NUMBER LIKE 'EP-%' OR pie.IC_NUMBER LIKE '%/EP-%') " +
            "AND (:vendorCode IS NULL OR ic.vendor_id = :vendorCode OR TRIM(LEADING ':' FROM ic.vendor_id) = TRIM(LEADING ':' FROM :vendorCode)) " +
            "AND (:plantParam IS NULL OR CAST(ic.unit_id AS CHAR) = :plantParam OR ic.unit_name = :plantParam OR ic.place_of_inspection = :plantParam) " +
            "GROUP BY pie.IC_NUMBER, ic.id, ic.po_no, ic.po_serial_no, ic.actual_inspection_date " +
            "ORDER BY pie.IC_NUMBER DESC",
            nativeQuery = true)
    List<Object[]> findEligibleProcessSourceIcs(
            @Param("vendorCode") String vendorCode,
            @Param("plantParam") String plantParam
    );

    @Query(value = "SELECT " +
            "COALESCE(NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(fie.IC_NUMBER, '/', 2), '/', -1), ''), fie.IC_NUMBER) AS certificate_no, " +
            "COALESCE(NULLIF(SUBSTRING_INDEX(SUBSTRING_INDEX(fie.IC_NUMBER, '/', 2), '/', -1), ''), fie.IC_NUMBER) AS call_no, " +
            "ic.po_no, " +
            "ic.po_serial_no, " +
            "ic.actual_inspection_date, " +
            "COALESCE((SELECT SUM(fld.qty_accepted) FROM final_inspection_lot_details fld JOIN final_inspection_details fid ON fld.final_detail_id = fid.id WHERE fid.ic_id = ic.id), " +
            "         (SELECT fid2.total_accepted_qty FROM final_inspection_details fid2 WHERE fid2.ic_id = ic.id), " +
            "         (SELECT fid3.total_offered_qty FROM final_inspection_details fid3 WHERE fid3.ic_id = ic.id), 0.0) AS total_accepted, " +
            "MAX(um.FULL_NAME) AS ie_name, " +
            "MAX(cs.BLOB_URL) AS cert_doc_url, " +
            "MAX(fie.CREATED_BY) AS ie_user_id " +
            "FROM FINAL_IC_EDIT fie " +
            "INNER JOIN inspection_calls ic ON (ic.ic_number COLLATE utf8mb4_unicode_ci = fie.IC_NUMBER COLLATE utf8mb4_unicode_ci OR ic.ic_number COLLATE utf8mb4_unicode_ci = SUBSTRING_INDEX(SUBSTRING_INDEX(fie.IC_NUMBER, '/', 2), '/', -1) COLLATE utf8mb4_unicode_ci) " +
            "LEFT JOIN user_master um ON CAST(um.USERID AS CHAR) COLLATE utf8mb4_unicode_ci = fie.CREATED_BY COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN certificate_storage cs ON (" +
            "    cs.IC_NUMBER COLLATE utf8mb4_unicode_ci = fie.IC_NUMBER COLLATE utf8mb4_unicode_ci " +
            "    OR cs.IC_NUMBER COLLATE utf8mb4_unicode_ci = ic.ic_number COLLATE utf8mb4_unicode_ci " +
            "    OR cs.IC_NUMBER COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            "    OR cs.FILE_NAME COLLATE utf8mb4_unicode_ci LIKE CONCAT('%', ic.ic_number COLLATE utf8mb4_unicode_ci, '%') " +
            ") " +
            "WHERE (fie.IC_NUMBER LIKE '%/EF-%/%' OR fie.IC_NUMBER LIKE 'EF-%' OR fie.IC_NUMBER LIKE '%/EF-%') " +
            "AND (:vendorCode IS NULL OR ic.vendor_id = :vendorCode OR TRIM(LEADING ':' FROM ic.vendor_id) = TRIM(LEADING ':' FROM :vendorCode)) " +
            "AND (:plantParam IS NULL OR CAST(ic.unit_id AS CHAR) = :plantParam OR ic.unit_name = :plantParam OR ic.place_of_inspection = :plantParam) " +
            "GROUP BY fie.IC_NUMBER, ic.id, ic.po_no, ic.po_serial_no, ic.actual_inspection_date " +
            "ORDER BY fie.IC_NUMBER DESC",
            nativeQuery = true)
    List<Object[]> findEligibleFinalSourceIcs(
            @Param("vendorCode") String vendorCode,
            @Param("plantParam") String plantParam
    );

    void deleteByCallNo(String normalizedRequestId);
}
