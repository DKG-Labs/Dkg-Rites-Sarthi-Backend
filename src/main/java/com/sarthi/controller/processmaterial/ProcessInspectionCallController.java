package com.sarthi.controller.processmaterial;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.IcDtos.CreateProcessInspectionCallRequestDto;
import com.sarthi.dto.po.PoDataForSectionsDto;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.entity.processmaterial.ProcessInspectionDetails;
import com.sarthi.entity.processmaterial.ProcessRmIcMapping;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.rawmaterial.InspectionCallRepository;
import com.sarthi.repository.processmaterial.ProcessInspectionDetailsRepository;
import com.sarthi.repository.processmaterial.ProcessRmIcMappingRepository;
import com.sarthi.service.ProcessInspectionCallService;
import com.sarthi.service.WorkflowService;
import com.sarthi.service.PoDataService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/process-material")
@CrossOrigin(origins = "*")
public class ProcessInspectionCallController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInspectionCallController.class);

    private final ProcessInspectionCallService processInspectionCallService;
    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private InspectionCallRepository inspectionCallRepository;

    @Autowired
    private ProcessInspectionDetailsRepository processInspectionDetailsRepository;

    @Autowired
    private ProcessRmIcMappingRepository processRmIcMappingRepository;

    @Autowired
    private PoDataService poDataService;

    @Autowired
    public ProcessInspectionCallController(
            ProcessInspectionCallService processInspectionCallService,
            WorkflowService workflowService
    ) {
        this.processInspectionCallService = processInspectionCallService;
        this.workflowService = workflowService;
    }

    /**
     * Create a new Process Inspection Call with Process details
     * POST /api/process-material/inspectionCall
     */
    @PostMapping("/inspectionCall")
    @Operation(summary = "Create Process inspection call", description = "Creates a new Process inspection call with lot-heat details")
    public ResponseEntity<APIResponse> createProcessInspectionCall(
            @RequestBody CreateProcessInspectionCallRequestDto request) {

        try {
            logger.info("========== CREATE PROCESS INSPECTION CALL REQUEST ==========");
            logger.info("Request object: {}", request);
            logger.info("Inspection Call: {}", request.getInspectionCall());
            logger.info("Process Details: {}", request.getProcessInspectionDetails());
            logger.info("====================================================");

            // 1️⃣ Save Process inspection call
            InspectionCall ic = processInspectionCallService.createProcessInspectionCall(
                    request.getInspectionCall(),
                    request.getProcessInspectionDetails()
            );


            logger.info("✅ Process inspection call created successfully with ID: {}", ic.getId());
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(ic),
                    HttpStatus.OK
            );

        } catch (Exception e) {

            logger.error("❌ ERROR creating Process inspection call", e);

            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.INTER_SERVER_ERROR,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    e.getMessage() != null ? e.getMessage() : "Failed to create Process inspection call"
            );

            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PatchMapping("/modifyInspectionCall")
    @Operation(summary = "Modify Process inspection call", description = "Modifies an existing Process inspection call")
    public ResponseEntity<Object> modifyProcessInspectionCall(
            @RequestParam String icNumber,
            @RequestBody CreateProcessInspectionCallRequestDto request) {
        logger.info("========== MODIFY PROCESS INSPECTION CALL REQUEST ==========");
        logger.info("IC Number: {}", icNumber);
        logger.info("Request object: {}", request);

        try {
            InspectionCall ic = processInspectionCallService.modifyProcessInspectionCall(
                    icNumber,
                    request.getInspectionCall(),
                    request.getProcessInspectionDetails()
            );

            logger.info("✅ Process inspection call modified successfully: {}", ic.getIcNumber());
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(ic),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            logger.error("❌ ERROR modifying Process inspection call", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.INTER_SERVER_ERROR,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    e.getMessage() != null ? e.getMessage() : "Failed to modify Process inspection call"
            );
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * Get Process inspection initiation data by call number
     * GET /api/process-material/inspection-call-details/{callNo}
     */
    @GetMapping("/inspection-call-details/{callNo}")
    @Operation(summary = "Get Process inspection initiation data", description = "Returns inspection call, process inspection details, RM IC mappings, and PO data for a call number")
    public ResponseEntity<Object> getProcessInspectionByCallNo(@PathVariable String callNo) {
        logger.info("GET /api/process-material/inspection-call-details/{} - Fetching process initiation data", callNo);

        InspectionCall ic = inspectionCallRepository.findByIcNumber(callNo)
                .orElse(null);

        if (ic == null) {
            logger.warn("Process initiation: Inspection call not found for callNo: {}", callNo);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Inspection call not found for callNo: " + callNo);
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
        }

        List<ProcessInspectionDetails> processDetails = processInspectionDetailsRepository.findByIcId(ic.getId().longValue());
        List<ProcessRmIcMapping> mappings = processRmIcMappingRepository.findByProcessIcId(ic.getId().longValue());

        // Fetch PO data from po_header and po_ma_header tables
        PoDataForSectionsDto poData = null;
        if (ic.getPoNo() != null && !ic.getPoNo().trim().isEmpty()) {
            try {
                logger.info("Fetching PO data for PO: {} and Call: {}", ic.getPoNo(), callNo);
                poData = poDataService.getPoDataWithRmDetailsForSectionC(ic.getPoNo(), callNo);
                if (poData != null) {
                    logger.info("✅ PO data fetched successfully for Process inspection");
                } else {
                    logger.warn("⚠️ No PO data found for PO: {}", ic.getPoNo());
                }
            } catch (Exception e) {
                logger.error("❌ Error fetching PO data for Process inspection: {}", e.getMessage());
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("inspectionCall", ic);
        resp.put("processInspectionDetails", processDetails);
        resp.put("processRmIcMappings", mappings);
        resp.put("poData", poData);

        return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(resp), HttpStatus.OK);
    }
}

