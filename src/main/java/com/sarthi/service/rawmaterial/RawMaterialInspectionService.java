package com.sarthi.service.rawmaterial;

import com.sarthi.dto.rawmaterial.InspectionCallDto;
import com.sarthi.dto.rawmaterial.RmHeatQuantityDto;
import com.sarthi.dto.rawmaterial.RmInspectionDetailsDto;
import com.sarthi.dto.rawmaterial.RmIcCertWithDateDto;

import java.util.List;

/**
 * Service interface for Raw Material Inspection operations.
 * Handles all CRUD and business logic for the 4 RM tables:
 * - inspection_calls
 * - rm_inspection_details
 * - rm_heat_quantities
 * - rm_chemical_analysis
 */
public interface RawMaterialInspectionService {

    /* ==================== Inspection Call Operations ==================== */

    /**
     * Get all Raw Material inspection calls
     * 
     * @return List of all RM inspection calls with basic details
     */
    List<InspectionCallDto> getAllRawMaterialCalls();

    /**
     * Get Raw Material calls by status
     * 
     * @param status Status filter (Pending, Scheduled, In Progress, Completed)
     * @return Filtered list of RM inspection calls
     */
    List<InspectionCallDto> getRawMaterialCallsByStatus(String status);

    /**
     * Get complete inspection call details by ID (includes all related data)
     * 
     * @param id Inspection call ID
     * @return Complete call details with heat quantities
     */
    InspectionCallDto getInspectionCallById(Integer id);

    /**
     * Get inspection call by IC number
     * 
     * @param icNumber Unique IC number
     * @return Call details
     */
    InspectionCallDto getInspectionCallByIcNumber(String icNumber);

    /* ==================== RM Inspection Details Operations ==================== */

    /**
     * Get RM inspection details by inspection call ID
     * 
     * @param inspectionCallId Parent call ID
     * @return RM-specific inspection details
     */
    RmInspectionDetailsDto getRmDetailsByCallId(Integer inspectionCallId);

    /* ==================== Heat Quantity Operations ==================== */

    /**
     * Get all heat quantities for an RM inspection detail
     * 
     * @param rmDetailId Parent RM detail ID
     * @return List of heat-wise quantity breakdown
     */
    List<RmHeatQuantityDto> getHeatQuantitiesByRmDetailId(Integer rmDetailId);

    /**
     * Get heat quantity by ID
     * 
     * @param heatId Heat quantity ID
     * @return Heat details
     */
    RmHeatQuantityDto getHeatQuantityById(Integer heatId);

    /* ==================== Process IC Support Operations ==================== */

    /**
     * Get completed RM IC certificate numbers for Process IC dropdown
     * Fetches certificate_no from inspection_complete_details table
     * Filters by ER prefix in call_no (Raw Material inspections) and PO Serial
     * Number
     * Used for Process IC dropdown when call type is "Process"
     * 
     * @param poSerialNo Purchase Order Serial Number to filter by (optional - if
     *                   null, returns all ER ICs)
     * @return List of completed RM IC certificate numbers (e.g.,
     *         "N/ER-01080001/RAJK")
     */
    List<RmIcCertWithDateDto> getCompletedRmIcNumbers(String poSerialNo);

    /**
     * Get heat numbers for a specific RM IC number
     * Fetches from rm_heat_quantities table based on the RM IC's ic_id
     * 
     * @param rmIcNumber RM IC Number (call_no from inspection_complete_details,
     *                   e.g., "ER-01080001")
     * @return List of heat numbers with details
     */
    List<RmHeatQuantityDto> getHeatNumbersByRmIcNumber(String rmIcNumber);

    /**
     * Get inspection call details by certificate number
     * Maps certificate number from inspection_complete_details to IC number, then
     * fetches inspection call details
     * Used for Process IC to fetch company_id, unit_id, and other details from the
     * RM IC
     * 
     * @param certificateNo Certificate number from inspection_complete_details
     *                      (e.g., "N/ER-0118005/RAJK")
     * @return Inspection call details including company_id, unit_id, company_name,
     *         unit_name, unit_address
     */
    InspectionCallDto getInspectionCallByCertificateNo(String certificateNo);

    /* ==================== Chemical Analysis Operations ==================== */

    /**
     * Get the most recent chemical analysis for a heat number
     * Fetches from rm_chemical_analysis table ordered by created_at DESC
     * Used to auto-populate chemical analysis when raising a new inspection call
     * for the same heat
     * 
     * @param heatNumber Heat number to search for
     * @return Chemical analysis data or null if not found
     */
    com.sarthi.dto.rawmaterial.RmChemicalAnalysisDto getChemicalAnalysisByHeatNumber(String heatNumber);

}
