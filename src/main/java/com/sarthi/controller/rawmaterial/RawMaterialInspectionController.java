package com.sarthi.controller.rawmaterial;

import com.sarthi.dto.IcDtos.InspectionCallRequestDto;
import com.sarthi.dto.IcDtos.RmInspectionDetailsRequestDto;
import com.sarthi.dto.WorkflowDtos.WorkflowTransitionDto;
import com.sarthi.dto.rawmaterial.*;
import com.sarthi.dto.vendorDtos.VendorPoHeaderResponseDto;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.constant.AppConstant;
import com.sarthi.dto.IcDtos.CreateInspectionCallRequestDto;
import com.sarthi.dto.rawmaterial.*;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.service.InspectionCallService;
import com.sarthi.service.WorkflowService;
import com.sarthi.service.rawmaterial.RawMaterialInspectionService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Raw Material Inspection operations.
 * All endpoints are JWT protected.
 *
 * Base URL: /api/raw-material
 */
@RestController
@RequestMapping("/api/raw-material")
@CrossOrigin(origins = "*")
@Tag(name = "Raw Material Inspection", description = "APIs for Raw Material inspection data")
@SecurityRequirement(name = "bearerAuth")
public class RawMaterialInspectionController {

    private static final Logger logger = LoggerFactory.getLogger(RawMaterialInspectionController.class);

    private final RawMaterialInspectionService rmService;

    @Autowired
    private InspectionCallService inspectionCallService;
    @Autowired
    private WorkflowService workflowService;

    @Autowired
    public RawMaterialInspectionController(RawMaterialInspectionService rmService) {
        this.rmService = rmService;
    }

    /* ==================== Inspection Call Endpoints ==================== */

    /**
     * Get all Raw Material inspection calls
     * GET /api/raw-material/calls
     */
    @GetMapping("/calls")
    @Operation(summary = "Get all RM inspection calls", description = "Fetches all Raw Material inspection calls with basic details")
    public ResponseEntity<APIResponse> getAllRawMaterialCalls() {
        logger.info("Request: Get all Raw Material inspection calls");
        List<InspectionCallDto> calls = rmService.getAllRawMaterialCalls();
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(calls), HttpStatus.OK);
    }

    /**
     * Get Raw Material calls by status
     * GET /api/raw-material/calls/status/{status}
     */
    @GetMapping("/calls/status/{status}")
    @Operation(summary = "Get RM calls by status", description = "Fetches Raw Material calls filtered by status")
    public ResponseEntity<APIResponse> getRawMaterialCallsByStatus(@PathVariable String status) {
        logger.info("Request: Get Raw Material calls by status: {}", status);
        List<InspectionCallDto> calls = rmService.getRawMaterialCallsByStatus(status.toUpperCase());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(calls), HttpStatus.OK);
    }

    /**
     * Get inspection call by ID (with full details)
     * GET /api/raw-material/calls/{id}
     */
    @GetMapping("/calls/{id}")
    @Operation(summary = "Get call by ID", description = "Fetches complete inspection call details including heats")
    public ResponseEntity<APIResponse> getInspectionCallById(@PathVariable Integer id) {
        logger.info("Request: Get inspection call by ID: {}", id);
        InspectionCallDto call = rmService.getInspectionCallById(id);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(call), HttpStatus.OK);
    }

    /**
     * Get inspection call by IC number
     * GET /api/raw-material/calls/ic-number/{icNumber}
     */
    @GetMapping("/calls/ic-number/{icNumber}")
    @Operation(summary = "Get call by IC number", description = "Fetches inspection call by unique IC number")
    public ResponseEntity<APIResponse> getInspectionCallByIcNumber(@PathVariable String icNumber) {
        logger.info("Request: Get inspection call by IC number: {}", icNumber);
        InspectionCallDto call = rmService.getInspectionCallByIcNumber(icNumber);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(call), HttpStatus.OK);
    }

    /* ==================== RM Inspection Details Endpoints ==================== */

    /**
     * Get RM inspection details by call ID
     * GET /api/raw-material/details/call/{callId}
     */
    @GetMapping("/details/call/{callId}")
    @Operation(summary = "Get RM details by call ID", description = "Fetches RM-specific inspection details for a call")
    public ResponseEntity<APIResponse> getRmDetailsByCallId(@PathVariable Integer callId) {
        logger.info("Request: Get RM details for call ID: {}", callId);
        RmInspectionDetailsDto details = rmService.getRmDetailsByCallId(callId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(details), HttpStatus.OK);
    }

    /* ==================== Heat Quantity Endpoints ==================== */

    /**
     * Get all heat quantities for an RM detail
     * GET /api/raw-material/heats/rm-detail/{rmDetailId}
     */
    @GetMapping("/heats/rm-detail/{rmDetailId}")
    @Operation(summary = "Get heats by RM detail ID", description = "Fetches all heat-wise quantity breakdown for an RM inspection detail")
    public ResponseEntity<APIResponse> getHeatQuantitiesByRmDetailId(@PathVariable Integer rmDetailId) {
        logger.info("Request: Get heat quantities for RM detail ID: {}", rmDetailId);
        List<RmHeatQuantityDto> heats = rmService.getHeatQuantitiesByRmDetailId(rmDetailId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(heats), HttpStatus.OK);
    }

    /**
     * Get heat quantity by ID
     * GET /api/raw-material/heats/{heatId}
     */
    @GetMapping("/heats/{heatId}")
    @Operation(summary = "Get heat by ID", description = "Fetches heat quantity details")
    public ResponseEntity<APIResponse> getHeatQuantityById(@PathVariable Integer heatId) {
        logger.info("Request: Get heat quantity by ID: {}", heatId);
        RmHeatQuantityDto heat = rmService.getHeatQuantityById(heatId);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(heat), HttpStatus.OK);
    }

    /* ==================== Process IC Support Endpoints ==================== */

    /**
     * Get completed RM IC certificate numbers for Process IC dropdown
     * GET /api/raw-material/completed-rm-ics?poSerialNo=xxx
     */
    @GetMapping("/completed-rm-ics")
    @Operation(summary = "Get completed RM ICs", description = "Fetches completed RM IC certificate numbers from inspection_complete_details table filtered by ER prefix and optionally by PO Serial Number")
    public ResponseEntity<APIResponse> getCompletedRmIcNumbers(@RequestParam(required = false) String poSerialNo) {
        logger.info("Request: Get completed RM IC certificate numbers for PO Serial No: {}", poSerialNo);
        List<String> completedRmIcs = rmService.getCompletedRmIcNumbers(poSerialNo);
        logger.info("Found {} completed RM IC certificate numbers", completedRmIcs.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(completedRmIcs), HttpStatus.OK);
    }

    /**
     * Get heat numbers by RM IC number
     * GET /api/raw-material/heats-by-rm-ic/{rmIcNumber}
     */
    @GetMapping("/heats-by-rm-ic/{rmIcNumber}")
    @Operation(summary = "Get heat numbers by RM IC", description = "Fetches heat numbers from rm_heat_quantities table for a specific RM IC number")
    public ResponseEntity<APIResponse> getHeatNumbersByRmIcNumber(@PathVariable String rmIcNumber) {
        logger.info("Request: Get heat numbers for RM IC: {}", rmIcNumber);
        List<RmHeatQuantityDto> heatNumbers = rmService.getHeatNumbersByRmIcNumber(rmIcNumber);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(heatNumbers), HttpStatus.OK);
    }

    /**
     * Get RM IC details by Certificate Number
     * Fetches inspection call details (company_id, unit_id, etc.) using the
     * certificate number
     * from inspection_complete_details table
     * GET /api/raw-material/ic-by-certificate/{certificateNo}
     */
    @GetMapping("/ic-by-certificate/{certificateNo}")
    @Operation(summary = "Get RM IC details by certificate number", description = "Fetches inspection call details using the certificate number from inspection_complete_details")
    public ResponseEntity<APIResponse> getRmIcDetailsByCertificateNo(@PathVariable String certificateNo) {
        logger.info("Request: Get RM IC details for certificate number: {}", certificateNo);
        InspectionCallDto call = rmService.getInspectionCallByCertificateNo(certificateNo);
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(call), HttpStatus.OK);
    }

    /* ==================== Chemical Analysis Endpoints ==================== */

    /**
     * Get chemical analysis by heat number
     * Fetches the most recent chemical analysis data for a given heat number
     * Used to auto-populate chemical analysis when raising a new inspection call
     * for the same heat
     * GET /api/raw-material/chemical-analysis/heat/{heatNumber}
     */
    @GetMapping("/chemical-analysis/heat/{heatNumber}")
    @Operation(summary = "Get chemical analysis by heat number", description = "Fetches the most recent chemical analysis for a heat number to auto-populate in new inspection calls")
    public ResponseEntity<APIResponse> getChemicalAnalysisByHeatNumber(@PathVariable String heatNumber) {
        logger.info("Request: Get chemical analysis for heat number: {}", heatNumber);
        com.sarthi.dto.rawmaterial.RmChemicalAnalysisDto analysis = rmService
                .getChemicalAnalysisByHeatNumber(heatNumber);

        if (analysis == null) {
            logger.info("No chemical analysis found for heat number: {}", heatNumber);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(null), HttpStatus.OK);
        }

        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(analysis), HttpStatus.OK);
    }

    /* ==================== Inspection Call Creation ==================== */

    // @PostMapping("/inspectionCall")
    // public ResponseEntity<Object> createInspectionCall(@RequestBody
    // InspectionCallRequestDto icRequest,
    // @RequestBody RmInspectionDetailsRequestDto rmRequest) {

    // InspectionCall ic = inspectionCallService.createInspectionCall(icRequest,
    // rmRequest);
    // return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(ic),
    // HttpStatus.OK);

    /**
     * Create a new inspection call with RM details
     * POST /api/raw-material/inspectionCall
     */
    @PostMapping("/inspectionCall")
    @Operation(summary = "Create inspection call", description = "Creates a new inspection call with RM inspection details")
    public ResponseEntity<APIResponse> createInspectionCall(
            @RequestBody CreateInspectionCallRequestDto request) {

        try {
            logger.info("========== CREATE INSPECTION CALL REQUEST ==========");
            logger.info("Request object: {}", request);
            logger.info("Inspection Call: {}", request.getInspectionCall());
            logger.info("RM Details: {}", request.getRmInspectionDetails());
            logger.info("====================================================");

            // 1️⃣ Save inspection call
            InspectionCall ic = inspectionCallService.createInspectionCall(
                    request.getInspectionCall(),
                    request.getRmInspectionDetails());

            // 2️⃣ Trigger workflow ONLY on success
            String workflowName = "INSPECTION CALL";

            Integer createdById = null;
            try {
                if (ic.getCreatedBy() != null && !ic.getCreatedBy().equalsIgnoreCase("system")) {
                    createdById = Integer.valueOf(ic.getCreatedBy());
                } else {
                    createdById = 3;
                }
            } catch (NumberFormatException nfe) {
                logger.warn("⚠️ Invalid createdBy value: '{}', using default 3", ic.getCreatedBy());
                createdById = 3;
            }

            try {
                workflowService.initiateWorkflow(
                        ic.getIcNumber(),
                        createdById,
                        workflowName,
                        "560001");
                logger.info("✅ Workflow initiated for IC: {}", ic.getIcNumber());
            } catch (Exception workflowEx) {
                logger.error("⚠️ Workflow initiation failed, but IC was created successfully: {}",
                        workflowEx.getMessage());
                // Don't throw exception - IC was created successfully in database
            }

            logger.info("✅ Inspection call created successfully with ID: {}", ic.getId());

            // Return simplified response to avoid potential serialization issues with
            // complex entities
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("id", ic.getId());
            responseData.put("icNumber", ic.getIcNumber());

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(responseData),
                    HttpStatus.OK);

        } catch (com.sarthi.exception.BusinessException be) {
            logger.error("❌ Business error creating inspection call: {}", be.getMessage());
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(be.getErrorDetails()),
                    HttpStatus.BAD_REQUEST);

        } catch (com.sarthi.exception.InvalidInputException iie) {
            logger.error("❌ Invalid input error creating inspection call: {}", iie.getMessage());
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(iie.getErrorDetails()),
                    HttpStatus.BAD_REQUEST);

        } catch (Exception e) {

            logger.error("❌ ERROR creating inspection call", e);

            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.INTER_SERVER_ERROR,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    e.getMessage() != null ? e.getMessage()
                            : "Failed to create inspection call: " + e.getClass().getSimpleName());

            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
