package com.sarthi.controller.finalmaterial;

import com.sarthi.dto.finalmaterial.FinalCumulativeResultsDto;
import com.sarthi.dto.finalmaterial.FinalInspectionSummaryDto;
import com.sarthi.dto.finalmaterial.FinalInspectionLotResultsDto;
import com.sarthi.service.finalmaterial.FinalDashboardResultsService;
import com.sarthi.util.ResponseBuilder;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.constant.AppConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * REST Controller for Final Dashboard Results
 * Handles saving and retrieving cumulative results and inspection summary data
 */
@RestController
@RequestMapping("/api/final-inspection/dashboard-results")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Final Dashboard Results", description = "APIs for Final Dashboard Results management")
public class FinalDashboardResultsController {

    private static final Logger logger = LoggerFactory.getLogger(FinalDashboardResultsController.class);
    private final FinalDashboardResultsService dashboardResultsService;

    // ===== CUMULATIVE RESULTS =====
    @PostMapping("/cumulative-results")
    @Operation(summary = "Save cumulative results")
    public ResponseEntity<?> saveCumulativeResults(
            @RequestBody FinalCumulativeResultsDto dto,
            Principal principal) {
        try {
            logger.info("Saving cumulative results for call: {}", dto.getInspectionCallNo());
            String userId = principal != null ? principal.getName() : "system";
            var result = dashboardResultsService.saveCumulativeResults(dto, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving cumulative results", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save cumulative results: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/cumulative-results/{callNo}")
    @Operation(summary = "Get cumulative results by call number")
    public ResponseEntity<?> getCumulativeResults(@PathVariable String callNo) {
        try {
            var result = dashboardResultsService.getCumulativeResultsByCallNo(callNo);
            if (result.isPresent()) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result.get()), HttpStatus.OK);
            }
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Cumulative results not found"
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error retrieving cumulative results", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve cumulative results"
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }
    }

    // ===== INSPECTION SUMMARY =====
    @PostMapping("/inspection-summary")
    @Operation(summary = "Save inspection summary")
    public ResponseEntity<?> saveInspectionSummary(
            @RequestBody FinalInspectionSummaryDto dto,
            Principal principal) {
        try {
            logger.info("Saving inspection summary for call: {}", dto.getInspectionCallNo());
            String userId = principal != null ? principal.getName() : "system";
            var result = dashboardResultsService.saveInspectionSummary(dto, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving inspection summary", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save inspection summary: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/inspection-summary/{callNo}")
    @Operation(summary = "Get inspection summary by call number")
    public ResponseEntity<?> getInspectionSummary(@PathVariable String callNo) {
        try {
            var result = dashboardResultsService.getInspectionSummaryByCallNo(callNo);
            if (result.isPresent()) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result.get()), HttpStatus.OK);
            }
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Inspection summary not found"
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error retrieving inspection summary", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve inspection summary"
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }
    }

    // ===== LOT RESULTS =====
    @PostMapping("/lot-results")
    @Operation(summary = "Save lot results")
    public ResponseEntity<?> saveLotResults(
            @RequestBody FinalInspectionLotResultsDto dto,
            Principal principal) {
        try {
            logger.info("Saving lot results for call: {} lot: {}", dto.getInspectionCallNo(), dto.getLotNo());
            String userId = principal != null ? principal.getName() : "system";
            var result = dashboardResultsService.saveLotResults(dto, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving lot results", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save lot results: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/lot-results/{callNo}/{lotNo}")
    @Operation(summary = "Get lot results by call number and lot number")
    public ResponseEntity<?> getLotResults(
            @PathVariable String callNo,
            @PathVariable String lotNo) {
        try {
            var result = dashboardResultsService.getLotResultsByCallNoAndLotNo(callNo, lotNo);
            if (result.isPresent()) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result.get()), HttpStatus.OK);
            }
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Lot results not found"
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error retrieving lot results", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve lot results"
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/lot-results/{callNo}")
    @Operation(summary = "Get all lot results for a call")
    public ResponseEntity<?> getAllLotResults(@PathVariable String callNo) {
        try {
            var results = dashboardResultsService.getLotResultsByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(results), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving lot results", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve lot results"
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/lot-results/sample-size")
    @Operation(summary = "Update sample size for a specific lot")
    public ResponseEntity<?> updateSampleSize(
            @RequestParam String callNo,
            @RequestParam String lotNo,
            @RequestParam Integer sampleSize,
            Principal principal) {
        try {
            logger.info("Updating sample size for call: {} lot: {} to {}", callNo, lotNo, sampleSize);
            String userId = principal != null ? principal.getName() : "system";
            var result = dashboardResultsService.updateSampleSize(callNo, lotNo, sampleSize, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating sample size", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update sample size: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        }
    }
}

