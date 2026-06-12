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
     * Find InspectionCompleteDetails by call number
     */
    Optional<InspectionCompleteDetails> findByCallNo(String callNo);

    /**
     * Find the most recent InspectionCompleteDetails by call number (in case of duplicates)
     */
    Optional<InspectionCompleteDetails> findFirstByCallNoOrderByCreatedOnDesc(String callNo);

    /**
     * Find InspectionCompleteDetails by certificate number
     * Used to map certificate number to IC number for Process IC
     */
    Optional<InspectionCompleteDetails> findByCertificateNo(String certificateNo);

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
     * Logic: rm_ic_number in process_inspection_details stores CERTIFICATE_NO directly
     */
    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "WHERE icd.CALL_NO IN (" +
            "    SELECT DISTINCT ic2.ic_number " +
            "    FROM process_inspection_details pid " +
            "    INNER JOIN inspection_calls ic2 ON pid.ic_id = ic2.id " +
            "    WHERE pid.rm_ic_number = :rmCertificateNo" +
            ") " +
            "AND icd.CALL_NO LIKE 'EP%' " +
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
            "    FROM process_inspection_details pid " +
            "    INNER JOIN inspection_calls ic2 ON pid.ic_id = ic2.id " +
            "    WHERE pid.rm_ic_number IN :rmCertificateNos" +
            ") " +
            "AND icd.CALL_NO LIKE 'EP%' " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<String> findProcessIcNumbersByMultipleRmIcNumbers(@Param("rmCertificateNos") List<String> rmCertificateNos);

    @Query("""
        SELECT i.certificateNo
        FROM InspectionCompleteDetails i
        WHERE i.callNo = :callNo
    """)
    String findCertificateNoByCallNo(
            @Param("callNo") String callNo);

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
            "LEFT JOIN rm_ic_edit rm ON icd.CERTIFICATE_NO = rm.ic_number " +
            "WHERE icd.CALL_NO LIKE 'ER-%' " +
            "AND ic.po_serial_no = :poSerialNo " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findCompletedRmIcCertificateNumbersWithDateByPoSerialNo(@Param("poSerialNo") String poSerialNo);

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(rm.created_at, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "LEFT JOIN rm_ic_edit rm ON icd.CERTIFICATE_NO = rm.ic_number " +
            "WHERE icd.CALL_NO LIKE 'ER-%' " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findAllCompletedRmIcsWithDate();

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(p.CREATED_AT, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "INNER JOIN inspection_calls ic ON icd.CALL_NO = ic.ic_number " +
            "LEFT JOIN PROCESS_IC_EDIT p ON icd.CERTIFICATE_NO = p.IC_NUMBER " +
            "WHERE icd.CALL_NO LIKE 'EP%' " +
            "AND ic.vendor_id = :vendorId " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findProcessIcCertificateNumbersWithDateByVendor(@Param("vendorId") String vendorId);

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(p.CREATED_AT, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "LEFT JOIN PROCESS_IC_EDIT p ON icd.CERTIFICATE_NO = p.IC_NUMBER " +
            "WHERE icd.CALL_NO IN (" +
            "    SELECT DISTINCT ic2.ic_number " +
            "    FROM process_inspection_details pid " +
            "    INNER JOIN inspection_calls ic2 ON pid.ic_id = ic2.id " +
            "    WHERE pid.rm_ic_number = :rmCertificateNo" +
            ") " +
            "AND icd.CALL_NO LIKE 'EP%' " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findProcessIcNumbersWithDateByRmIcNumber(@Param("rmCertificateNo") String rmCertificateNo);

    @Query(value = "SELECT DISTINCT icd.CERTIFICATE_NO AS certificateNo, COALESCE(p.CREATED_AT, icd.CREATED_ON) AS createdAt " +
            "FROM inspection_complete_details icd " +
            "LEFT JOIN PROCESS_IC_EDIT p ON icd.CERTIFICATE_NO = p.IC_NUMBER " +
            "WHERE icd.CALL_NO IN (" +
            "    SELECT DISTINCT ic2.ic_number " +
            "    FROM process_inspection_details pid " +
            "    INNER JOIN inspection_calls ic2 ON pid.ic_id = ic2.id " +
            "    WHERE pid.rm_ic_number IN :rmCertificateNos" +
            ") " +
            "AND icd.CALL_NO LIKE 'EP%' " +
            "ORDER BY icd.CERTIFICATE_NO DESC",
            nativeQuery = true)
    List<Object[]> findProcessIcNumbersWithDateByMultipleRmIcNumbers(@Param("rmCertificateNos") List<String> rmCertificateNos);

}
