package com.sarthi.controller.processmaterial;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.IcDtos.CreateProcessInspectionCallRequestDto;
import com.sarthi.entity.rawmaterial.InspectionCall;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.service.ProcessInspectionCallService;
import com.sarthi.service.WorkflowService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/process-material")
@CrossOrigin(origins = "*")
public class ProcessInspectionCallController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInspectionCallController.class);

    private final ProcessInspectionCallService processInspectionCallService;
    @Autowired
    private WorkflowService workflowService;

    @Autowired
    public ProcessInspectionCallController(
            ProcessInspectionCallService processInspectionCallService,
            WorkflowService workflowService) {
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
                    request.getProcessInspectionDetails());

            // 2️⃣ Trigger workflow ONLY on success
            String workflowName = "INSPECTION CALL";
            try {
                Integer createdByUserId = null;
                try {
                    if (ic.getCreatedBy() != null && !ic.getCreatedBy().equalsIgnoreCase("system")) {
                        createdByUserId = Integer.valueOf(ic.getCreatedBy());
                    } else {
                        createdByUserId = 3;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("⚠️ createdBy is not a valid integer: {}. using default 3.", ic.getCreatedBy());
                    createdByUserId = 3;
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
                logger.error("⚠️ Workflow initiation failed, but IC was created successfully: {}",
                        workflowEx.getMessage());
                // Don't throw exception - IC was created successfully
            }

            logger.info("✅ Process inspection call created successfully with ID: {}", ic.getId());

            // Return simplified response to avoid potential serialization issues with
            // complex entities
            java.util.Map<String, Object> responseData = new java.util.HashMap<>();
            responseData.put("id", ic.getId());
            responseData.put("icNumber", ic.getIcNumber());

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(responseData),
                    HttpStatus.OK);

        } catch (com.sarthi.exception.BusinessException be) {
            logger.error("❌ Business error creating Process inspection call: {}", be.getMessage());
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(be.getErrorDetails()),
                    HttpStatus.BAD_REQUEST);

        } catch (com.sarthi.exception.InvalidInputException iie) {
            logger.error("❌ Invalid input error creating Process inspection call: {}", iie.getMessage());
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(iie.getErrorDetails()),
                    HttpStatus.BAD_REQUEST);

        } catch (Exception e) {

            logger.error("❌ ERROR creating Process inspection call", e);

            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.INTER_SERVER_ERROR,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    e.getMessage() != null ? e.getMessage()
                            : "Failed to create Process inspection call: " + e.getClass().getSimpleName());

            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
