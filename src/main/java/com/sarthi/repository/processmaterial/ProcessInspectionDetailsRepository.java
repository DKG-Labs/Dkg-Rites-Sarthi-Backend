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
     * rm_ic_number in process_inspection_details stores CERTIFICATE_NO directly
     */
    @Query(value = "SELECT DISTINCT pid.lot_number " +
            "FROM process_inspection_details pid " +
            "INNER JOIN inspection_calls ic ON pid.ic_id = ic.id " +
            "INNER JOIN inspection_complete_details icd_process ON ic.ic_number = icd_process.CALL_NO " +
            "WHERE pid.rm_ic_number = :rmCertificateNo " +
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
    @Query(value = "SELECT DISTINCT heat_number " +
            "FROM process_inspection_details " +
            "WHERE lot_number = :lotNumber " +
            "AND rm_ic_number = :rmCertificateNo " +
            "AND heat_number IS NOT NULL " +
            "ORDER BY heat_number",
            nativeQuery = true)
    List<String> findHeatNumbersByLotNumber(
            @Param("lotNumber") String lotNumber,
            @Param("rmCertificateNo") String rmCertificateNo);

    /**
     * Find lot numbers by multiple RM IC certificates and multiple Process IC certificates
     * Returns all unique lot numbers that match any combination of the specified certificates
     */
    @Query(value = "SELECT DISTINCT pid.lot_number " +
            "FROM process_inspection_details pid " +
            "INNER JOIN inspection_calls ic ON pid.ic_id = ic.id " +
            "INNER JOIN inspection_complete_details icd_process ON ic.ic_number = icd_process.CALL_NO " +
            "WHERE pid.rm_ic_number IN :rmCertificateNos " +
            "AND icd_process.CERTIFICATE_NO IN :processCertificateNos " +
            "AND pid.lot_number IS NOT NULL " +
            "ORDER BY pid.lot_number",
            nativeQuery = true)
    List<String> findLotNumbersByMultipleRmAndProcessIcNumbers(
            @Param("rmCertificateNos") List<String> rmCertificateNos,
            @Param("processCertificateNos") List<String> processCertificateNos);


}

