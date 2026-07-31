package com.sarthi.service;

import com.sarthi.dto.IcDtos.FinalInspectionDetailsRequestDto;
import com.sarthi.dto.IcDtos.FinalInspectionLotDetailsRequestDto;
import com.sarthi.dto.IcDtos.InspectionCallRequestDto;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.dto.rawmaterial.RmIcCertWithDateDto;
import com.sarthi.dto.processmaterial.ProcessIcCertWithDateDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service interface for Final Inspection Call operations
 */
@Service
public interface FinalInspectionCallService {

    /**
     * Create a new Final Inspection Call
     * 
     * @param icRequest      - Inspection Call basic details
     * @param finalDetails   - Final Inspection summary details
     * @param lotDetailsList - List of Final Inspection Lot Details
     * @return Created InspectionCall entity
     */
    InspectionCall createFinalInspectionCall(
            InspectionCallRequestDto icRequest,
            FinalInspectionDetailsRequestDto finalDetails,
            List<FinalInspectionLotDetailsRequestDto> lotDetailsList);

    InspectionCall modifyFinalInspectionCall(
            String icNumber,
            InspectionCallRequestDto icDto,
            FinalInspectionDetailsRequestDto finalDto,
            List<FinalInspectionLotDetailsRequestDto> lotDtoList);

    /**
     * Get Process IC certificate numbers for Final Inspection Call dropdown
     * Filters by vendor and EP prefix
     * 
     * @param vendorId - Vendor ID to filter
     * @return List of certificate numbers
     */
    List<ProcessIcCertWithDateDto> getProcessIcCertificateNumbers(String vendorId);

    /**
     * Get RM IC numbers by Process IC certificate number
     * 
     * @param certificateNo - Process IC certificate number
     * @return List of RM IC numbers
     */
    List<String> getRmIcNumbersByCertificateNo(String certificateNo);

    /**
     * Get Lot numbers by RM IC number
     * 
     * @param rmIcNumber - RM IC number
     * @return List of lot numbers
     */
    List<String> getLotNumbersByRmIcNumber(String rmIcNumber);

    // ==================== NEW METHODS FOR REVERSED DROPDOWN FLOW
    // ====================

    /**
     * Get RM IC certificate numbers for Final Inspection Call dropdown
     * Returns CERTIFICATE_NO (e.g., "N/ER-01120005/RAJK") for display
     * 
     * @param poSerialNo - PO Serial Number to filter
     * @return List of RM IC certificate numbers
     */
    List<RmIcCertWithDateDto> getRmIcCertificateNumbers(String poSerialNo);

    /**
     * Get Process IC certificate numbers by RM IC certificate
     * Returns CERTIFICATE_NO (e.g., "N/EP-01170002/RAJK") for Process ICs that used
     * the specified RM IC
     * 
     * @param rmCertificateNo - RM IC certificate number (e.g.,
     *                        "N/ER-01120005/RAJK")
     * @return List of Process IC certificate numbers
     */
    List<ProcessIcCertWithDateDto> getProcessIcCertificateNumbersByRmCertificate(String rmCertificateNo);

    /**
     * Get Lot numbers by RM IC and Process IC certificate numbers
     * Converts certificates to IC numbers internally, then finds lots
     * 
     * @param rmCertificateNo      - RM IC certificate number (e.g.,
     *                             "N/ER-01120005/RAJK")
     * @param processCertificateNo - Process IC certificate number (e.g.,
     *                             "N/EP-01170002/RAJK")
     * @return List of lot numbers
     */
    List<String> getLotNumbersByRmAndProcessCertificates(String rmCertificateNo, String processCertificateNo);

    /**
     * Get Heat numbers by lot number and RM IC certificate
     * Returns heat numbers for a specific lot that matches the RM IC certificate
     * 
     * @param lotNumber       - Lot number
     * @param rmCertificateNo - RM IC certificate number
     * @return List of heat numbers
     */
    List<String> getHeatNumbersByLotNumber(String lotNumber, String rmCertificateNo, String processCertificateNo);

    /**
     * Get Process IC certificate numbers for multiple RM IC certificates
     * Returns all unique Process IC certificates that used any of the specified RM
     * ICs
     * 
     * @param rmCertificateNos - List of RM IC certificate numbers
     * @return List of Process IC certificate numbers
     */
    List<ProcessIcCertWithDateDto> getProcessIcCertificateNumbersByMultipleRmCertificates(List<String> rmCertificateNos);

    /**
     * Get Lot numbers by multiple RM IC and Process IC certificate numbers
     * Returns all unique lot numbers that match any combination of the specified
     * certificates
     * 
     * @param rmCertificateNos      - List of RM IC certificate numbers
     * @param processCertificateNos - List of Process IC certificate numbers
     * @return List of lot numbers
     */
    List<String> getLotNumbersByMultipleRmAndProcessCertificates(List<String> rmCertificateNos,
            List<String> processCertificateNos);

    /**
     * Get the total quantity offered in previous Final Inspection Calls for a
     * specific heat and lot
     * 
     * @param heatNo    - Heat number
     * @param lotNumber - Lot number
     * @return Sum of offered quantity
     */
    Integer getOfferedEarlierQuantity(String heatNo, String lotNumber);
}
