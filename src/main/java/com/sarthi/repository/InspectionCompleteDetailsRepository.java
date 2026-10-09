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

    @Query(value = "SELECT DISTINCT " +
            "icd.CERTIFICATE_NO, " +
            "icd.CALL_NO, " +
            "ic.po_no, " +
            "ic.po_serial_no, " +
            "ic.actual_inspection_date, " +
            "COALESCE((SELECT SUM(fr.weight_accepted_mt) FROM rm_heat_final_result fr WHERE fr.inspection_call_no = icd.CALL_NO), " +
            "         (SELECT SUM(hq.qty_accepted) FROM rm_heat_quantities hq JOIN rm_inspection_details rmd ON hq.rm_detail_id = rmd.id WHERE rmd.ic_id = ic.id), 0.0) AS total_accepted " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "WHERE icd.CALL_NO LIKE 'ER-%' " +
            "AND (:vendorCode IS NULL OR ic.vendor_id = :vendorCode OR TRIM(LEADING ':' FROM ic.vendor_id) = TRIM(LEADING ':' FROM :vendorCode)) " +
            "AND (:plantParam IS NULL OR CAST(ic.unit_id AS CHAR) = :plantParam OR ic.unit_name = :plantParam) " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findEligibleRmSourceIcs(
            @Param("vendorCode") String vendorCode,
            @Param("plantParam") String plantParam
    );

    @Query(value = "SELECT DISTINCT " +
            "icd.CERTIFICATE_NO, " +
            "icd.CALL_NO, " +
            "ic.po_no, " +
            "ic.po_serial_no, " +
            "ic.actual_inspection_date, " +
            "COALESCE((SELECT SUM(pfr.total_accepted) FROM process_line_final_result pfr WHERE pfr.inspection_call_no = icd.CALL_NO), " +
            "         (SELECT SUM(pid.qty_accepted) FROM process_inspection_details pid WHERE pid.ic_id = ic.id), " +
            "         (SELECT SUM(pid2.offered_qty) FROM process_inspection_details pid2 WHERE pid2.ic_id = ic.id), 0.0) AS total_accepted " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "WHERE (icd.CALL_NO LIKE 'EP-%' OR ic.type_of_call LIKE '%PROCESS%') " +
            "AND (:vendorCode IS NULL OR ic.vendor_id = :vendorCode OR TRIM(LEADING ':' FROM ic.vendor_id) = TRIM(LEADING ':' FROM :vendorCode)) " +
            "AND (:plantParam IS NULL OR CAST(ic.unit_id AS CHAR) = :plantParam OR ic.unit_name = :plantParam) " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findEligibleProcessSourceIcs(
            @Param("vendorCode") String vendorCode,
            @Param("plantParam") String plantParam
    );

    @Query(value = "SELECT DISTINCT " +
            "icd.CERTIFICATE_NO, " +
            "icd.CALL_NO, " +
            "ic.po_no, " +
            "ic.po_serial_no, " +
            "ic.actual_inspection_date, " +
            "COALESCE((SELECT SUM(fld.qty_accepted) FROM final_inspection_lot_details fld JOIN final_inspection_details fid ON fld.final_detail_id = fid.id WHERE fid.ic_id = ic.id), " +
            "         (SELECT fid2.total_accepted_qty FROM final_inspection_details fid2 WHERE fid2.ic_id = ic.id), " +
            "         (SELECT fid3.total_offered_qty FROM final_inspection_details fid3 WHERE fid3.ic_id = ic.id), 0.0) AS total_accepted " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "WHERE (icd.CALL_NO LIKE 'EF-%' OR ic.type_of_call LIKE '%FINAL%') " +
            "AND (:vendorCode IS NULL OR ic.vendor_id = :vendorCode OR TRIM(LEADING ':' FROM ic.vendor_id) = TRIM(LEADING ':' FROM :vendorCode)) " +
            "AND (:plantParam IS NULL OR CAST(ic.unit_id AS CHAR) = :plantParam OR ic.unit_name = :plantParam) " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findEligibleFinalSourceIcs(
            @Param("vendorCode") String vendorCode,
            @Param("plantParam") String plantParam
    );

    void deleteByCallNo(String normalizedRequestId);
}
