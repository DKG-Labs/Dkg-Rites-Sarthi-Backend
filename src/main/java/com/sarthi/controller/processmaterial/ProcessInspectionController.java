package com.sarthi.controller.processmaterial;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.processmaterial.ProcessFinishInspectionDto;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.service.ProcessInspectionService;
import com.sarthi.util.APIResponse;
import com.sarthi.util.ResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Process Material Inspection operations.
 * Handles finish, pause, and retrieve inspection data.
 */
@RestController
@RequestMapping("/api/process-material")
@CrossOrigin(origins = "*")
@Tag(name = "Process Material Inspection", description = "APIs for Process Material inspection operations")
public class ProcessInspectionController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessInspectionController.class);

    @Autowired
    private ProcessInspectionService service;

    /**
     * Finish Process Material inspection - saves all submodule data and marks as COMPLETED.
     * Called when inspector clicks "Finish Inspection" button.
     * POST /api/process-material/finish
     */
    @PostMapping("/finish")
    @Operation(summary = "Finish Process inspection", description = "Saves all inspection data and marks inspection as COMPLETED")
    public ResponseEntity<APIResponse> finishInspection(
            @RequestBody ProcessFinishInspectionDto dto,
            @RequestParam(required = false) String userId) {
        
        String callNo = dto.getInspectionCallNo();
        
        // Prioritize userId from request body (createdBy field) over query parameter
        String user = (userId != null && !userId.isEmpty()) ? userId : 
                     (dto.getCreatedBy() != null && !dto.getCreatedBy().isEmpty()) ? dto.getCreatedBy() : "SYSTEM";
        
        logger.info("POST /api/process-material/finish - Finishing inspection for call: {} by user: {}", callNo, user);
        
        try {
            String result = service.finishInspection(dto, user);
            logger.info("✅ Process inspection finished successfully for call: {}", callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Validation errors - return BAD_REQUEST with clear message
            logger.warn("⚠️ Validation error for Process inspection: {}", e.getMessage());
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.USER_INVALID_INPUT,
                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Unexpected errors - return INTERNAL_SERVER_ERROR
            logger.error("❌ Error finishing Process inspection for call: {}", callNo, e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.ERROR_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_INTERNAL,
                "Failed to finish inspection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Pause Process Material inspection - saves all submodule data WITHOUT changing status.
     * Called when inspector clicks "Pause Inspection" button.
     * POST /api/process-material/pause
     */
    @PostMapping("/pause")
    @Operation(summary = "Pause Process inspection", description = "Saves all inspection data without changing status")
    public ResponseEntity<APIResponse> pauseInspection(
            @RequestBody ProcessFinishInspectionDto dto,
            @RequestParam(required = false) String userId) {
        
        String callNo = dto.getInspectionCallNo();
        
        // Prioritize userId from request body (createdBy field) over query parameter
        String user = (userId != null && !userId.isEmpty()) ? userId : 
                     (dto.getCreatedBy() != null && !dto.getCreatedBy().isEmpty()) ? dto.getCreatedBy() : "SYSTEM";
        
        logger.info("POST /api/process-material/pause - Pausing inspection for call: {} by user: {}", callNo, user);
        
        try {
            String result = service.pauseInspection(dto, user);
            logger.info("✅ Process inspection paused successfully for call: {}", callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Validation errors - return BAD_REQUEST with clear message
            logger.warn("⚠️ Validation error for Process inspection pause: {}", e.getMessage());
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.USER_INVALID_INPUT,
                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Unexpected errors - return INTERNAL_SERVER_ERROR
            logger.error("❌ Error pausing Process inspection for call: {}", callNo, e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.ERROR_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_INTERNAL,
                "Failed to pause inspection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all Process Material inspection data by call number.
     * Used when revisiting an inspection to load previously saved data.
     * GET /api/process-material/inspection/{callNo}
     */
    @GetMapping("/inspection/{callNo}")
    @Operation(summary = "Get Process inspection data", description = "Retrieves all inspection data for a call number")
    public ResponseEntity<APIResponse> getInspectionByCallNo(@PathVariable String callNo) {
        logger.info("GET /api/process-material/inspection/{} - Fetching inspection data", callNo);
        
        try {
            ProcessFinishInspectionDto data = service.getByCallNo(callNo);
            if (data == null) {
                logger.warn("⚠️ No inspection data found for call: {}", callNo);
                ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Inspection data not found for call: " + callNo
                );
                return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
            }
            logger.info("✅ Inspection data retrieved successfully for call: {}", callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("❌ Error fetching inspection data for call: {}", callNo, e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.ERROR_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_INTERNAL,
                "Failed to fetch inspection data: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get final inspection results for all production lines for a given inspection call.
     * Used for manufactured quantity validation across stages.
     * GET /api/process-material/final-results/{callNo}
     */
    @GetMapping("/final-results/{callNo}")
    @Operation(summary = "Get Process final results", description = "Retrieves final inspection results for all lines of a call number")
    public ResponseEntity<APIResponse> getFinalResultsByCallNo(@PathVariable String callNo) {
        logger.info("GET /api/process-material/final-results/{} - Fetching final results", callNo);
        
        try {
            var results = service.getFinalResultsByCallNo(callNo);
            if (results == null || results.isEmpty()) {
                logger.warn("⚠️ No final results found for call: {}", callNo);
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(results), HttpStatus.OK); // Return empty list
            }
            logger.info("✅ Final results retrieved successfully for call: {}", callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(results), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("❌ Error fetching final results for call: {}", callNo, e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.ERROR_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_INTERNAL,
                "Failed to fetch final results: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get sum of accepted quantities for all stages for a given inspection call and lot number.
     * GET /api/process-material/accepted-quantities
     */
    @GetMapping("/accepted-quantities")
    @Operation(summary = "Get accepted quantities sum", description = "Retrieves sum of accepted quantities for all stages of a call number and lot number")
    public ResponseEntity<APIResponse> getAcceptedQuantities(
            @RequestParam String callNo,
            @RequestParam String lotNo) {
        logger.info("GET /api/process-material/accepted-quantities - Fetching accepted quantities sum for call: {}, lot: {}", callNo, lotNo);
        
        try {
            var result = service.getAcceptedQuantitySum(callNo, lotNo);
            if (result == null) {
                logger.warn("⚠️ No accepted quantities found for call: {}, lot: {}", callNo, lotNo);
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(null), HttpStatus.OK);
            }
            logger.info("✅ Accepted quantities sum retrieved successfully for call: {}, lot: {}", callNo, lotNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("❌ Error fetching accepted quantities sum for call: {}, lot: {}", callNo, lotNo, e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.ERROR_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_INTERNAL,
                "Failed to fetch accepted quantities sum: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

