package com.sarthi.controller.finalmaterial;

import com.sarthi.dto.IcDtos.CreateFinalInspectionCallRequestDto;
import com.sarthi.dto.po.PoDataForSectionsDto;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.util.ResponseBuilder;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.constant.AppConstant;
import com.sarthi.service.FinalInspectionCallService;
import com.sarthi.service.WorkflowService;
import com.sarthi.service.PoDataService;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.finalmaterial.FinalInspectionDetails;
import com.sarthi.entity.finalmaterial.FinalInspectionLotDetails;
import com.sarthi.entity.finalmaterial.FinalProcessIcMapping;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalInspectionLotDetailsRepository;
import com.sarthi.repository.finalmaterial.FinalProcessIcMappingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Final Inspection Call operations
 */
@RestController
@RequestMapping("/api/final-material")
@CrossOrigin(origins = "*")
@Tag(name = "Final Inspection Call", description = "APIs for Final Inspection Call management")
public class FinalInspectionCallController {

    private static final Logger logger = LoggerFactory.getLogger(FinalInspectionCallController.class);

    @Autowired
    private FinalInspectionCallService finalInspectionCallService;

    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private PoDataService poDataService;

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private FinalInspectionDetailsRepository finalInspectionDetailsRepository;

    @Autowired
    private FinalInspectionLotDetailsRepository finalInspectionLotDetailsRepository;

    @Autowired
    private FinalProcessIcMappingRepository finalProcessIcMappingRepository;

    /**
     * Create a new Final Inspection Call with lot details
     * POST /api/final-material/inspectionCall
     */
    @PostMapping("/inspectionCall")
    @Operation(summary = "Create Final inspection call", description = "Creates a new Final inspection call with lot details")
    public ResponseEntity<Object> createFinalInspectionCall(
            @RequestBody CreateFinalInspectionCallRequestDto request) {

        logger.info("========== CREATE FINAL INSPECTION CALL REQUEST ==========");
        logger.info("Request object: {}", request);
        logger.info("Inspection Call: {}", request.getInspectionCall());
        logger.info("Final Details: {}", request.getFinalInspectionDetails());
        logger.info("Lot Details: {}", request.getFinalLotDetails());
        logger.info("====================================================");

        // 1️⃣ Save Final inspection call
        InspectionCall ic = finalInspectionCallService.createFinalInspectionCall(
                request.getInspectionCall(),
                request.getFinalInspectionDetails(),
                request.getFinalLotDetails());

        // 2️⃣ Trigger workflow ONLY on success
        String workflowName = "INSPECTION CALL";
        try {
            // Try to parse createdBy as Integer, if it fails, use a default value or skip
            // workflow
            Integer createdByUserId = null;
            try {
                createdByUserId = Integer.valueOf(ic.getCreatedBy());
            } catch (NumberFormatException e) {
                logger.warn("⚠️ createdBy is not a valid integer: {}. Skipping workflow initiation.",
                        ic.getCreatedBy());
            }

            if (createdByUserId != null) {
                workflowService.initiateWorkflow(
                        ic.getIcNumber(),
                        createdByUserId,
                        workflowName,
                        "560001");
                logger.info("✅ Workflow initiated for IC: {}", ic.getIcNumber());
            }
        } catch (Exception workflowEx) {
            logger.error("⚠️ Workflow initiation failed but IC was created: {}", workflowEx.getMessage());
            // Don't fail the entire request if workflow fails
        }

        // 3️⃣ Return success response
        InspectionCallResponse responseData = new InspectionCallResponse(
                ic.getId(),
                ic.getIcNumber(),
                "Final Inspection Call created successfully");

        logger.info("✅ Final Inspection Call created successfully: {}", ic.getIcNumber());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(responseData), HttpStatus.CREATED);
    }

    /**
     * Get final inspection initiation data by call number
     * GET /api/final-material/inspection/{callNo}
     *
     * Enhanced to fetch PO data from po_header and po_ma_header tables
     * similar to Raw Material implementation
     */
    @GetMapping("/inspection/{callNo}")
    @Operation(summary = "Get Final inspection initiation data", description = "Returns inspection call, final details, lot details, process mappings, and PO data for a call number")
    public ResponseEntity<Object> getFinalInspectionByCallNo(@PathVariable String callNo) {
        logger.info("GET /api/final-material/inspection/{} - Fetching final initiation data", callNo);

        InspectionCall ic = inspectionCallRepository.findByIcNumber(callNo)
                .orElse(null);

        if (ic == null) {
            logger.warn("Final initiation: Inspection call not found for callNo: {}", callNo);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Inspection call not found for callNo: " + callNo);
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
        }

        FinalInspectionDetails finalDetails = finalInspectionDetailsRepository.findByIcId(ic.getId().longValue())
                .orElse(null);

        java.util.List<FinalInspectionLotDetails> lotDetails = new java.util.ArrayList<>();
        java.util.List<FinalProcessIcMapping> mappings = new java.util.ArrayList<>();

        if (finalDetails != null) {
            lotDetails = finalInspectionLotDetailsRepository.findByFinalDetailId(finalDetails.getId());
        }

        mappings = finalProcessIcMappingRepository.findByFinalIcId(ic.getId().longValue());

        // Fetch PO data from po_header and po_ma_header tables (similar to Raw
        // Material)
        PoDataForSectionsDto poData = null;
        if (ic.getPoNo() != null && !ic.getPoNo().trim().isEmpty()) {
            try {
                logger.info("Fetching PO data for PO: {} and Call: {}", ic.getPoNo(), callNo);
                poData = poDataService.getPoDataWithRmDetailsForSectionC(ic.getPoNo(), callNo);
                if (poData != null) {
                    logger.info("✅ PO data fetched successfully for Final inspection");
                } else {
                    logger.warn("⚠️ No PO data found for PO: {}", ic.getPoNo());
                }
            } catch (Exception e) {
                logger.error("❌ Error fetching PO data for Final inspection: {}", e.getMessage());
                // Continue without PO data - frontend will use fallback
            }
        }

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("inspectionCall", ic);
        resp.put("finalInspectionDetails", finalDetails);
        resp.put("finalLotDetails", lotDetails);
        resp.put("finalProcessMappings", mappings);
        resp.put("poData", poData); // Add PO data to response

        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(resp), HttpStatus.OK);
    }

    /**
     * Get Final Product Dashboard data by call number
     * Optimized endpoint that returns all dashboard data in one call
     * GET /api/final-material/dashboard/{callNo}
     */
    @GetMapping("/dashboard/{callNo}")
    @Operation(summary = "Get Final Product Dashboard data", description = "Returns all data needed for the Final Product Dashboard in a single optimized call")
    public ResponseEntity<Object> getFinalDashboardData(@PathVariable String callNo) {
        logger.info("GET /api/final-material/dashboard/{} - Fetching dashboard data", callNo);

        try {
            InspectionCall ic = inspectionCallRepository.findByIcNumber(callNo)
                    .orElse(null);

            if (ic == null) {
                logger.warn("Dashboard: Inspection call not found for callNo: {}", callNo);
                ErrorDetails errorDetails = new ErrorDetails(
                        AppConstant.ERROR_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_CODE_RESOURCE,
                        AppConstant.ERROR_TYPE_RESOURCE,
                        "Inspection call not found for callNo: " + callNo);
                return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
            }

            FinalInspectionDetails finalDetails = finalInspectionDetailsRepository.findByIcId(ic.getId().longValue())
                    .orElse(null);

            java.util.List<FinalInspectionLotDetails> lotDetails = new java.util.ArrayList<>();
            if (finalDetails != null) {
                lotDetails = finalInspectionLotDetailsRepository.findByFinalDetailId(finalDetails.getId());
            }

            // Fetch PO data
            PoDataForSectionsDto poData = null;
            if (ic.getPoNo() != null && !ic.getPoNo().trim().isEmpty()) {
                try {
                    logger.info("Fetching PO data for PO: {} and Call: {}", ic.getPoNo(), callNo);
                    poData = poDataService.getPoDataWithRmDetailsForSectionC(ic.getPoNo(), callNo);
                    if (poData != null) {
                        logger.info("✅ PO data fetched successfully for Final dashboard");
                    } else {
                        logger.warn("⚠️ No PO data found for PO: {}", ic.getPoNo());
                    }
                } catch (Exception e) {
                    logger.error("❌ Error fetching PO data for Final dashboard: {}", e.getMessage());
                }
            }

            // Build optimized response with only required fields
            java.util.Map<String, Object> dashboardData = new java.util.HashMap<>();
            dashboardData.put("inspectionCall", ic);
            dashboardData.put("finalInspectionDetails", finalDetails);
            dashboardData.put("finalLotDetails", lotDetails);
            dashboardData.put("poData", poData);

            logger.info("✅ Dashboard data fetched successfully for call: {}", callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dashboardData), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("❌ Error fetching dashboard data: {}", e.getMessage());
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Error fetching dashboard data: " + e.getMessage());
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get Process IC certificate numbers for Final Inspection Call dropdown
     * GET /api/final-material/process-ic-certificates?vendorId=xxx
     */
    @GetMapping("/process-ic-certificates")
    @Operation(summary = "Get Process IC certificate numbers", description = "Get certificate numbers for Process ICs (EP prefix) filtered by vendor")
    public ResponseEntity<Object> getProcessIcCertificates(@RequestParam String vendorId) {
        logger.info("Fetching Process IC certificates for vendor: {}", vendorId);
        List<String> certificates = finalInspectionCallService.getProcessIcCertificateNumbers(vendorId);
        logger.info("Found {} certificates", certificates.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(certificates), HttpStatus.OK);
    }

    /**
     * Get RM IC numbers by Process IC certificate number
     * GET /api/final-material/rm-ic-numbers?certificateNo=xxx
     */
    @GetMapping("/rm-ic-numbers")
    @Operation(summary = "Get RM IC numbers by Process IC", description = "Get RM IC numbers for a given Process IC certificate number")
    public ResponseEntity<Object> getRmIcNumbers(@RequestParam String certificateNo) {
        logger.info("Fetching RM IC numbers for certificate: {}", certificateNo);
        List<String> rmIcNumbers = finalInspectionCallService.getRmIcNumbersByCertificateNo(certificateNo);
        logger.info("Found {} RM IC numbers", rmIcNumbers.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(rmIcNumbers), HttpStatus.OK);
    }

    /**
     * Get Lot numbers by RM IC number
     * GET /api/final-material/lot-numbers?rmIcNumber=xxx
     */
    @GetMapping("/lot-numbers")
    @Operation(summary = "Get Lot numbers by RM IC", description = "Get lot numbers for a given RM IC number")
    public ResponseEntity<Object> getLotNumbers(@RequestParam String rmIcNumber) {
        logger.info("Fetching lot numbers for RM IC: {}", rmIcNumber);
        List<String> lotNumbers = finalInspectionCallService.getLotNumbersByRmIcNumber(rmIcNumber);
        logger.info("Found {} lot numbers", lotNumbers.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(lotNumbers), HttpStatus.OK);
    }

    // ==================== NEW ENDPOINTS FOR REVERSED DROPDOWN FLOW
    // ====================

    /**
     * Get RM IC certificate numbers for Final Inspection Call dropdown
     * Returns CERTIFICATE_NO (e.g., "N/ER-01120005/RAJK") for display
     * GET /api/final-material/rm-ic-certificates?poSerialNo=xxx
     */
    @GetMapping("/rm-ic-certificates")
    @Operation(summary = "Get RM IC certificate numbers", description = "Get RM IC certificate numbers from completed inspections, filtered by PO Serial Number")
    public ResponseEntity<Object> getRmIcCertificates(@RequestParam String poSerialNo) {
        logger.info("Fetching RM IC certificate numbers for PO Serial No: {}", poSerialNo);
        List<String> certificateNumbers = finalInspectionCallService.getRmIcCertificateNumbers(poSerialNo);
        logger.info("Found {} RM IC certificate numbers", certificateNumbers.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(certificateNumbers), HttpStatus.OK);
    }

    /**
     * Get Process IC certificate numbers by RM IC certificate
     * Returns CERTIFICATE_NO (e.g., "N/EP-01170002/RAJK") for Process ICs that used
     * the specified RM IC
     * GET /api/final-material/process-ic-by-rm?rmCertificateNo=xxx
     */
    @GetMapping("/process-ic-by-rm")
    @Operation(summary = "Get Process IC certificates by RM IC", description = "Get Process IC certificate numbers that used the given RM IC certificate")
    public ResponseEntity<Object> getProcessIcByRmCertificate(@RequestParam String rmCertificateNo) {
        logger.info("Fetching Process IC certificate numbers for RM certificate: {}", rmCertificateNo);
        List<String> certificateNumbers = finalInspectionCallService
                .getProcessIcCertificateNumbersByRmCertificate(rmCertificateNo);
        logger.info("Found {} Process IC certificate numbers", certificateNumbers.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(certificateNumbers), HttpStatus.OK);
    }

    /**
     * Get Lot numbers by RM IC and Process IC certificate numbers
     * Converts certificates to IC numbers internally, then finds lots
     * GET
     * /api/final-material/lot-numbers-by-certificates?rmCertificateNo=xxx&processCertificateNo=yyy
     */
    @GetMapping("/lot-numbers-by-certificates")
    @Operation(summary = "Get Lot numbers by RM and Process IC certificates", description = "Get lot numbers for given RM IC and Process IC certificate numbers")
    public ResponseEntity<Object> getLotNumbersByCertificates(
            @RequestParam String rmCertificateNo,
            @RequestParam String processCertificateNo) {
        logger.info("Fetching lot numbers for RM certificate: {} and Process certificate: {}", rmCertificateNo,
                processCertificateNo);
        List<String> lotNumbers = finalInspectionCallService.getLotNumbersByRmAndProcessCertificates(rmCertificateNo,
                processCertificateNo);
        logger.info("Found {} lot numbers", lotNumbers.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(lotNumbers), HttpStatus.OK);
    }

    /**
     * Get Heat numbers by lot number and RM IC certificate (NEW FLOW)
     * GET /api/final-material/heat-numbers-by-lot?lotNumber=xxx&rmCertificateNo=yyy
     */
    @GetMapping("/heat-numbers-by-lot")
    @Operation(summary = "Get Heat numbers by Lot and RM IC", description = "Get heat numbers for a given lot number and RM IC certificate")
    public ResponseEntity<Object> getHeatNumbersByLot(
            @RequestParam String lotNumber,
            @RequestParam String rmCertificateNo) {
        logger.info("Fetching heat numbers for lot: {} and RM certificate: {}", lotNumber, rmCertificateNo);
        List<String> heatNumbers = finalInspectionCallService.getHeatNumbersByLotNumber(lotNumber, rmCertificateNo);
        logger.info("Found {} heat numbers", heatNumbers.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(heatNumbers), HttpStatus.OK);
    }

    /**
     * Get Process IC certificate numbers for multiple RM IC certificates
     * GET
     * /api/final-material/process-ic-by-multiple-rm?rmCertificateNos=xxx,yyy,zzz
     */
    @GetMapping("/process-ic-by-multiple-rm")
    @Operation(summary = "Get Process IC certificates by multiple RM ICs", description = "Get Process IC certificate numbers for multiple RM IC certificates")
    public ResponseEntity<Object> getProcessIcByMultipleRmCertificates(
            @RequestParam List<String> rmCertificateNos) {
        logger.info("Fetching Process IC certificate numbers for multiple RM certificates: {}", rmCertificateNos);
        List<String> certificateNumbers = finalInspectionCallService
                .getProcessIcCertificateNumbersByMultipleRmCertificates(rmCertificateNos);
        logger.info("Found {} Process IC certificate numbers", certificateNumbers.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(certificateNumbers), HttpStatus.OK);
    }

    /**
     * Get Lot numbers for multiple RM IC and Process IC certificates
     * GET
     * /api/final-material/lot-numbers-by-multiple-certificates?rmCertificateNos=xxx,yyy&processCertificateNos=aaa,bbb
     */
    @GetMapping("/lot-numbers-by-multiple-certificates")
    @Operation(summary = "Get Lot numbers by multiple RM and Process IC certificates", description = "Get lot numbers for multiple RM IC and Process IC certificate combinations")
    public ResponseEntity<Object> getLotNumbersByMultipleCertificates(
            @RequestParam List<String> rmCertificateNos,
            @RequestParam List<String> processCertificateNos) {
        logger.info("Fetching lot numbers for multiple RM certificates: {} and Process certificates: {}",
                rmCertificateNos, processCertificateNos);
        List<String> lotNumbers = finalInspectionCallService
                .getLotNumbersByMultipleRmAndProcessCertificates(rmCertificateNos, processCertificateNos);
        logger.info("Found {} lot numbers", lotNumbers.size());
        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(lotNumbers), HttpStatus.OK);
    }

    /**
     * Inner class for response
     */
    private static class InspectionCallResponse {
        public Long inspection_call_id;
        public String ic_number;
        public String message;

        public InspectionCallResponse(Long id, String icNumber, String message) {
            this.inspection_call_id = id;
            this.ic_number = icNumber;
            this.message = message;
        }
    }
}
