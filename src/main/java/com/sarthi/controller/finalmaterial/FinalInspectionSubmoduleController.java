package com.sarthi.controller.finalmaterial;

import com.sarthi.entity.finalmaterial.*;
import com.sarthi.service.finalmaterial.FinalInspectionSubmoduleService;
import com.sarthi.service.finalmaterial.FinalHardnessTestService;
import com.sarthi.service.finalmaterial.FinalToeLoadTestService;
import com.sarthi.service.finalmaterial.FinalWeightTestService;
import com.sarthi.dto.finalmaterial.FinalLadleValuesDto;
import com.sarthi.dto.finalmaterial.FinalInclusionRatingBatchDTO;
import com.sarthi.dto.finalmaterial.FinalHardnessTestRequest;
import com.sarthi.dto.finalmaterial.FinalHardnessTestResponse;
import com.sarthi.dto.finalmaterial.FinalToeLoadTestRequest;
import com.sarthi.dto.finalmaterial.FinalToeLoadTestResponse;
import com.sarthi.dto.finalmaterial.FinalWeightTestRequest;
import com.sarthi.dto.finalmaterial.FinalWeightTestResponse;
import com.sarthi.dto.finalmaterial.FinalDepthOfDecarburizationRequest;
import com.sarthi.dto.finalmaterial.FinalDepthOfDecarburizationResponse;
import com.sarthi.dto.finalmaterial.FinalInclusionRatingNewRequest;
import com.sarthi.dto.finalmaterial.FinalInclusionRatingNewResponse;
import com.sarthi.dto.finalmaterial.FinalMicrostructureTestRequest;
import com.sarthi.dto.finalmaterial.FinalMicrostructureTestResponse;
import com.sarthi.dto.finalmaterial.FinalFreedomFromDefectsTestRequest;
import com.sarthi.dto.finalmaterial.FinalFreedomFromDefectsTestResponse;
import com.sarthi.dto.finalmaterial.FinalChemicalAnalysisRequest;
import com.sarthi.dto.finalmaterial.FinalChemicalAnalysisResponse;
import com.sarthi.dto.finalmaterial.FinalDimensionalInspectionRequest;
import com.sarthi.dto.finalmaterial.FinalDimensionalInspectionResponse;
import com.sarthi.dto.finalmaterial.FinalDimensionalInspectionFlatDto;
import com.sarthi.dto.finalmaterial.FinalApplicationDeflectionRequest;
import com.sarthi.dto.finalmaterial.FinalApplicationDeflectionResponse;
import com.sarthi.service.finalmaterial.FinalDepthOfDecarburizationService;
import com.sarthi.service.finalmaterial.FinalInclusionRatingNewService;
import com.sarthi.service.finalmaterial.FinalMicrostructureTestService;
import com.sarthi.service.finalmaterial.FinalFreedomFromDefectsTestService;
import com.sarthi.service.finalmaterial.FinalDimensionalInspectionNewService;
import com.sarthi.service.finalmaterial.FinalDimensionalInspectionFlatService;
import com.sarthi.service.finalmaterial.FinalApplicationDeflectionNewService;
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
import java.util.List;

/**
 * REST Controller for Final Inspection Submodules
 * Handles CRUD operations for all final inspection submodule data
 */
@RestController
@RequestMapping("/api/final-inspection/submodules")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Final Inspection Submodules", description = "APIs for Final Inspection Submodule data management")
public class FinalInspectionSubmoduleController {

    private static final Logger logger = LoggerFactory.getLogger(FinalInspectionSubmoduleController.class);
    private final FinalInspectionSubmoduleService submoduleService;
    private final FinalHardnessTestService hardnessTestService;
    private final FinalToeLoadTestService toeLoadTestService;
    private final FinalWeightTestService weightTestService;
    private final FinalDepthOfDecarburizationService depthOfDecarburizationService;
    private final FinalInclusionRatingNewService inclusionRatingNewService;
    private final FinalMicrostructureTestService microstructureTestService;
    private final FinalFreedomFromDefectsTestService freedomFromDefectsTestService;
    private final FinalDimensionalInspectionNewService dimensionalInspectionNewService;
    private final FinalDimensionalInspectionFlatService dimensionalInspectionFlatService;
    private final FinalApplicationDeflectionNewService applicationDeflectionNewService;

    /**
     * Helper method to get userId from payload or principal
     * Priority: payload createdBy > principal > "system"
     */
    private String getUserId(Object data, Principal principal) {
        try {
            // Try to get createdBy from the entity using reflection
            java.lang.reflect.Field createdByField = data.getClass().getDeclaredField("createdBy");
            createdByField.setAccessible(true);
            String createdBy = (String) createdByField.get(data);
            if (createdBy != null && !createdBy.isEmpty()) {
                return createdBy;
            }
        } catch (Exception e) {
            // Field doesn't exist or can't be accessed, continue to next option
        }

        // Fallback to principal
        if (principal != null) {
            return principal.getName();
        }

        // Final fallback
        return "system";
    }

    // ===== CALIBRATION & DOCUMENTS =====
    @PostMapping("/calibration-documents")
    @Operation(summary = "Save Calibration & Documents data")
    public ResponseEntity<?> saveCalibrationDocuments(
            @RequestBody FinalCalibrationDocuments data,
            Principal principal) {
        try {
            String userId = getUserId(data, principal);
            FinalCalibrationDocuments saved = submoduleService.saveCalibrationDocuments(data, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(saved), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving calibration documents", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save calibration documents: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/calibration-documents/call/{callNo}")
    @Operation(summary = "Get Calibration & Documents by Call Number")
    public ResponseEntity<?> getCalibrationDocumentsByCall(@PathVariable String callNo) {
        try {
            List<FinalCalibrationDocuments> data = submoduleService.getCalibrationDocumentsByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving calibration documents", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve calibration documents: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/calibration-documents/call/{callNo}/lot/{lotNo}")
    @Operation(summary = "Get Calibration & Documents by Call and Lot Number")
    public ResponseEntity<?> getCalibrationDocumentsByLot(
            @PathVariable String callNo,
            @PathVariable String lotNo) {
        try {
            List<FinalCalibrationDocuments> data = submoduleService.getCalibrationDocumentsByLot(callNo, lotNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving calibration documents", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve calibration documents: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/calibration-documents/{id}")
    @Operation(summary = "Update Calibration & Documents data")
    public ResponseEntity<?> updateCalibrationDocuments(
            @PathVariable Long id,
            @RequestBody FinalCalibrationDocuments data,
            Principal principal) {
        try {
            String userId = principal.getName();
            data.setId(id);
            FinalCalibrationDocuments updated = submoduleService.updateCalibrationDocuments(data, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updated), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating calibration documents", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update calibration documents: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/calibration-documents/{id}")
    @Operation(summary = "Delete Calibration & Documents data")
    public ResponseEntity<?> deleteCalibrationDocuments(@PathVariable Long id) {
        try {
            submoduleService.deleteCalibrationDocuments(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Calibration documents deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting calibration documents", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete calibration documents: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== VISUAL INSPECTION =====
    // DEPRECATED: Use FinalVisualInspectionController instead
    // Endpoints moved to: /api/final-material/visual-inspection

    // ===== CHEMICAL ANALYSIS =====
    @PostMapping("/chemical-analysis")
    @Operation(summary = "Save Chemical Analysis data")
    public ResponseEntity<?> saveChemicalAnalysis(
            @RequestBody FinalChemicalAnalysisRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            // Priority: createdBy from request > X-User-Id header > principal > SYSTEM
            String userIdToUse = request.getCreatedBy() != null && !request.getCreatedBy().isEmpty()
                    ? request.getCreatedBy()
                    : (userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM"));
            FinalChemicalAnalysisResponse saved = submoduleService.saveChemicalAnalysis(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(saved), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving chemical analysis data", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save chemical analysis data: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/chemical-analysis/call/{callNo}")
    @Operation(summary = "Get Chemical Analysis by Call Number")
    public ResponseEntity<?> getChemicalAnalysisByCall(@PathVariable String callNo) {
        try {
            List<FinalChemicalAnalysis> data = submoduleService.getChemicalAnalysisByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving chemical analysis data", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve chemical analysis data: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/chemical-analysis/call/{callNo}/lot/{lotNo}")
    @Operation(summary = "Get Chemical Analysis by Call and Lot Number")
    public ResponseEntity<?> getChemicalAnalysisByLot(
            @PathVariable String callNo,
            @PathVariable String lotNo) {
        try {
            List<FinalChemicalAnalysis> data = submoduleService.getChemicalAnalysisByLot(callNo, lotNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving chemical analysis data", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve chemical analysis data: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/chemical-analysis/{id}")
    @Operation(summary = "Update Chemical Analysis data")
    public ResponseEntity<?> updateChemicalAnalysis(
            @PathVariable Long id,
            @RequestBody FinalChemicalAnalysis data,
            Principal principal) {
        try {
            String userId = principal.getName();
            data.setId(id);
            FinalChemicalAnalysis updated = submoduleService.updateChemicalAnalysis(data, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updated), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating chemical analysis data", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update chemical analysis data: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/chemical-analysis/{id}")
    @Operation(summary = "Delete Chemical Analysis data")
    public ResponseEntity<?> deleteChemicalAnalysis(@PathVariable Long id) {
        try {
            submoduleService.deleteChemicalAnalysis(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Chemical analysis data deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting chemical analysis data", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete chemical analysis data: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== HARDNESS TEST (NEW TWO-TABLE DESIGN) =====
    /**
     * Save or update hardness test with samples.
     * Supports both first save (create) and subsequent saves (pause/resume).
     */
    @PostMapping("/hardness-test")
    @Operation(summary = "Save or update hardness test inspection")
    public ResponseEntity<?> saveOrUpdateHardnessTest(
            @RequestBody FinalHardnessTestRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            // Priority: createdBy from request > X-User-Id header > principal > SYSTEM
            String userIdToUse = request.getCreatedBy() != null && !request.getCreatedBy().isEmpty()
                    ? request.getCreatedBy()
                    : (userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM"));
            FinalHardnessTestResponse response = hardnessTestService.saveOrUpdateHardnessTest(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving hardness test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save hardness test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get hardness test by inspection call, lot, and heat.
     */
    @GetMapping("/hardness-test/call/{callNo}/lot/{lotNo}/heat/{heatNo}")
    @Operation(summary = "Get hardness test by call, lot, and heat")
    public ResponseEntity<?> getHardnessTest(
            @PathVariable String callNo,
            @PathVariable String lotNo,
            @PathVariable String heatNo) {
        try {
            var response = hardnessTestService.getHardnessTest(callNo, lotNo, heatNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving hardness test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve hardness test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all hardness tests for an inspection call.
     */
    @GetMapping("/hardness-test/call/{callNo}")
    @Operation(summary = "Get all hardness tests for a call")
    public ResponseEntity<?> getHardnessTestsByCall(@PathVariable String callNo) {
        try {
            List<FinalHardnessTestResponse> response = hardnessTestService.getHardnessTestsByCall(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving hardness tests", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve hardness tests: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get hardness test by ID.
     */
    @GetMapping("/hardness-test/{id}")
    @Operation(summary = "Get hardness test by ID")
    public ResponseEntity<?> getHardnessTestById(@PathVariable Long id) {
        try {
            var response = hardnessTestService.getHardnessTestById(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving hardness test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve hardness test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update status of hardness test.
     */
    @PatchMapping("/hardness-test/{id}/status")
    @Operation(summary = "Update hardness test status")
    public ResponseEntity<?> updateHardnessTestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            // Priority: X-User-Id header > principal > SYSTEM
            String userIdToUse = userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM");
            FinalHardnessTestResponse response = hardnessTestService.updateStatus(id, status, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating hardness test status", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update status: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete hardness test.
     */
    @DeleteMapping("/hardness-test/{id}")
    @Operation(summary = "Delete hardness test")
    public ResponseEntity<?> deleteHardnessTest(@PathVariable Long id) {
        try {
            hardnessTestService.deleteHardnessTest(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Hardness test deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting hardness test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== TOE LOAD TEST (NEW TWO-TABLE DESIGN) =====
    /**
     * Save or update toe load test with samples.
     * Supports both first save (create) and subsequent saves (pause/resume).
     */
    @PostMapping("/toe-load-test")
    @Operation(summary = "Save or update toe load test inspection")
    public ResponseEntity<?> saveOrUpdateToeLoadTest(
            @RequestBody FinalToeLoadTestRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            // Priority: X-User-Id header > principal > SYSTEM
            String userIdToUse = request.getCreatedBy() != null ? request.getCreatedBy() : (principal != null ? principal.getName() : "SYSTEM");
            FinalToeLoadTestResponse response = toeLoadTestService.saveOrUpdateToeLoadTest(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving toe load test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save toe load test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get toe load test by inspection call, lot, and heat.
     */
    @GetMapping("/toe-load-test/call/{callNo}/lot/{lotNo}/heat/{heatNo}")
    @Operation(summary = "Get toe load test by call, lot, and heat")
    public ResponseEntity<?> getToeLoadTest(
            @PathVariable String callNo,
            @PathVariable String lotNo,
            @PathVariable String heatNo) {
        try {
            var response = toeLoadTestService.getToeLoadTest(callNo, lotNo, heatNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving toe load test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve toe load test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all toe load tests for an inspection call.
     */
    @GetMapping("/toe-load-test/call/{callNo}")
    @Operation(summary = "Get all toe load tests for a call")
    public ResponseEntity<?> getToeLoadTestsByCall(@PathVariable String callNo) {
        try {
            List<FinalToeLoadTestResponse> response = toeLoadTestService.getToeLoadTestsByCall(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving toe load tests", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve toe load tests: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get toe load test by ID.
     */
    @GetMapping("/toe-load-test/{id}")
    @Operation(summary = "Get toe load test by ID")
    public ResponseEntity<?> getToeLoadTestById(@PathVariable Long id) {
        try {
            var response = toeLoadTestService.getToeLoadTestById(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving toe load test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve toe load test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update status of toe load test.
     */
    @PatchMapping("/toe-load-test/{id}/status")
    @Operation(summary = "Update toe load test status")
    public ResponseEntity<?> updateToeLoadTestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Principal principal) {
        try {
            String userId = principal.getName();
            FinalToeLoadTestResponse response = toeLoadTestService.updateStatus(id, status, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating toe load test status", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update status: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete toe load test.
     */
    @DeleteMapping("/toe-load-test/{id}")
    @Operation(summary = "Delete toe load test")
    public ResponseEntity<?> deleteToeLoadTest(@PathVariable Long id) {
        try {
            toeLoadTestService.deleteToeLoadTest(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Toe load test deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting toe load test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== INCLUSION & DECARB - DEPRECATED =====
    // Old flat structure endpoints removed - use new parent-child structure endpoints below
    // Use /inclusion-rating-new, /depth-of-decarburization, /microstructure-test, /freedom-from-defects-test

    // ===== APPLICATION DEFLECTION (NEW TWO-TABLE DESIGN) =====
    /**
     * Save or update application & deflection test with samples.
     * Supports both first save (create) and subsequent saves (pause/resume).
     */
    @PostMapping("/application-deflection")
    @Operation(summary = "Save or update application & deflection test")
    public ResponseEntity<?> saveOrUpdateApplicationDeflection(
            @RequestBody FinalApplicationDeflectionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            // Priority: createdBy from request > X-User-Id header > principal > SYSTEM
            String userIdToUse = request.getCreatedBy() != null && !request.getCreatedBy().isEmpty()
                    ? request.getCreatedBy()
                    : (userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM"));
            FinalApplicationDeflectionResponse response = applicationDeflectionNewService
                    .saveOrUpdateApplicationDeflection(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving application deflection", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save application deflection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/application-deflection/call/{callNo}")
    @Operation(summary = "Get application & deflection tests by call number")
    public ResponseEntity<?> getApplicationDeflectionByCallNo(@PathVariable String callNo) {
        try {
            List<FinalApplicationDeflectionResponse> response = applicationDeflectionNewService
                    .getApplicationDeflectionsByCall(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving application deflection tests", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve application deflection tests: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/application-deflection/{id}/status")
    @Operation(summary = "Update application & deflection test status")
    public ResponseEntity<?> updateApplicationDeflectionStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            // Priority: X-User-Id header > principal > SYSTEM
            String userIdToUse = userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM");
            FinalApplicationDeflectionResponse response = applicationDeflectionNewService
                    .updateStatus(id, status, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating application deflection status", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update status: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/application-deflection/{id}")
    @Operation(summary = "Delete application & deflection test")
    public ResponseEntity<?> deleteApplicationDeflection(@PathVariable Long id) {
        try {
            applicationDeflectionNewService.deleteApplicationDeflection(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Application deflection deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting application deflection", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete application deflection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // // ===== WEIGHT TEST =====
    // @PostMapping("/weight-test")
    // @Operation(summary = "Save Weight Test data")
    // public ResponseEntity<?> saveWeightTest(
    //         @RequestBody FinalWeightTest data,
    //         Principal principal) {
    //     try {
    //         String userId = getUserId(data, principal);
    //         FinalWeightTest saved = submoduleService.saveWeightTest(data, userId);
    //         return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(saved), HttpStatus.CREATED);
    //     } catch (Exception e) {
    //         logger.error("Error saving weight test data", e);
    //         ErrorDetails errorDetails = new ErrorDetails(
    //                 AppConstant.ERROR_CODE_RESOURCE,
    //                 AppConstant.ERROR_TYPE_CODE_RESOURCE,
    //                 AppConstant.ERROR_TYPE_RESOURCE,
    //                 "Failed to save weight test data: " + e.getMessage()
    //         );
    //         return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
    //     }
    // }

    // ===== WEIGHT TEST (NEW TWO-TABLE DESIGN) =====
    /**
     * Save or update weight test with samples.
     * Supports both first save (create) and subsequent saves (pause/resume).
     */
    @PostMapping("/weight-test")
    @Operation(summary = "Save or update weight test inspection")
    public ResponseEntity<?> saveOrUpdateWeightTest(
            @RequestBody FinalWeightTestRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            // Priority: X-User-Id header > principal > SYSTEM
            String userIdToUse = request.getCreatedBy() != null ? request.getCreatedBy() : (principal != null ? principal.getName() : "SYSTEM");
            FinalWeightTestResponse response = weightTestService.saveOrUpdateWeightTest(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving weight test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save weight test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get weight test by inspection call, lot, and heat.
     */
    @GetMapping("/weight-test/call/{callNo}/lot/{lotNo}/heat/{heatNo}")
    @Operation(summary = "Get weight test by call, lot, and heat")
    public ResponseEntity<?> getWeightTest(
            @PathVariable String callNo,
            @PathVariable String lotNo,
            @PathVariable String heatNo) {
        try {
            var response = weightTestService.getWeightTest(callNo, lotNo, heatNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving weight test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve weight test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all weight tests for an inspection call.
     */
    @GetMapping("/weight-test/call/{callNo}")
    @Operation(summary = "Get all weight tests for a call")
    public ResponseEntity<?> getWeightTestsByCall(@PathVariable String callNo) {
        try {
            List<FinalWeightTestResponse> response = weightTestService.getWeightTestsByCall(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving weight tests", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve weight tests: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get weight test by ID.
     */
    @GetMapping("/weight-test/{id}")
    @Operation(summary = "Get weight test by ID")
    public ResponseEntity<?> getWeightTestById(@PathVariable Long id) {
        try {
            var response = weightTestService.getWeightTestById(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving weight test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve weight test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update status of weight test.
     */
    @PatchMapping("/weight-test/{id}/status")
    @Operation(summary = "Update weight test status")
    public ResponseEntity<?> updateWeightTestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Principal principal) {
        try {
            String userId = principal.getName();
            FinalWeightTestResponse response = weightTestService.updateStatus(id, status, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating weight test status", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update status: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete weight test.
     */
    @DeleteMapping("/weight-test/{id}")
    @Operation(summary = "Delete weight test")
    public ResponseEntity<?> deleteWeightTest(@PathVariable Long id) {
        try {
            weightTestService.deleteWeightTest(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Weight test deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting weight test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== TOE LOAD TEST =====
    // ===== LADLE VALUES =====
    @GetMapping("/ladle-values/{callNo}")
    @Operation(summary = "Get Ladle Values by Call Number")
    public ResponseEntity<?> getLadleValuesByCall(@PathVariable String callNo) {
        try {
            List<FinalLadleValuesDto> data = submoduleService.getLadleValuesByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(data), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving ladle values", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve ladle values: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== DEPTH OF DECARBURIZATION (NEW TWO-TABLE DESIGN) =====
    @PostMapping("/depth-of-decarburization")
    @Operation(summary = "Save or update depth of decarburization test")
    public ResponseEntity<?> saveOrUpdateDepthOfDecarburization(
            @RequestBody FinalDepthOfDecarburizationRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            String userIdToUse = request.getCreatedBy() != null && !request.getCreatedBy().isEmpty()
                    ? request.getCreatedBy()
                    : (userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM"));
            FinalDepthOfDecarburizationResponse response = depthOfDecarburizationService
                    .saveOrUpdateDepthOfDecarburization(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving depth of decarburization test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save depth of decarburization test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/depth-of-decarburization/{id}")
    @Operation(summary = "Get depth of decarburization test by ID")
    public ResponseEntity<?> getDepthOfDecarburizationById(@PathVariable Long id) {
        try {
            var response = depthOfDecarburizationService.getDepthOfDecarburizationById(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving depth of decarburization test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve depth of decarburization test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/depth-of-decarburization/call/{callNo}")
    @Operation(summary = "Get depth of decarburization tests by call number")
    public ResponseEntity<?> getDepthOfDecarburizationByCallNo(@PathVariable String callNo) {
        try {
            List<FinalDepthOfDecarburizationResponse> response = depthOfDecarburizationService
                    .getDepthOfDecarburizationByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving depth of decarburization tests", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve depth of decarburization tests: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/depth-of-decarburization/{id}/status")
    @Operation(summary = "Update depth of decarburization test status")
    public ResponseEntity<?> updateDepthOfDecarburizationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            String userIdToUse = userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM");
            FinalDepthOfDecarburizationResponse response = depthOfDecarburizationService
                    .updateStatus(id, status, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating depth of decarburization test status", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update depth of decarburization test status: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/depth-of-decarburization/{id}")
    @Operation(summary = "Delete depth of decarburization test")
    public ResponseEntity<?> deleteDepthOfDecarburization(@PathVariable Long id) {
        try {
            depthOfDecarburizationService.deleteDepthOfDecarburization(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Depth of decarburization test deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting depth of decarburization test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete depth of decarburization test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== INCLUSION RATING (NEW TWO-TABLE DESIGN) =====
    @PostMapping("/inclusion-rating-new")
    @Operation(summary = "Save or update inclusion rating test")
    public ResponseEntity<?> saveOrUpdateInclusionRating(
            @RequestBody FinalInclusionRatingNewRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            String userIdToUse = request.getCreatedBy() != null && !request.getCreatedBy().isEmpty()
                    ? request.getCreatedBy()
                    : (userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM"));
            FinalInclusionRatingNewResponse response = inclusionRatingNewService
                    .saveOrUpdateInclusionRating(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving inclusion rating test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save inclusion rating test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/inclusion-rating-new/{id}")
    @Operation(summary = "Get inclusion rating test by ID")
    public ResponseEntity<?> getInclusionRatingById(@PathVariable Long id) {
        try {
            var response = inclusionRatingNewService.getInclusionRatingById(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving inclusion rating test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve inclusion rating test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/inclusion-rating-new/call/{callNo}")
    @Operation(summary = "Get inclusion rating tests by call number")
    public ResponseEntity<?> getInclusionRatingByCallNo(@PathVariable String callNo) {
        try {
            List<FinalInclusionRatingNewResponse> response = inclusionRatingNewService
                    .getInclusionRatingByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving inclusion rating tests", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve inclusion rating tests: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/inclusion-rating-new/{id}/status")
    @Operation(summary = "Update inclusion rating test status")
    public ResponseEntity<?> updateInclusionRatingStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            String userIdToUse = userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM");
            FinalInclusionRatingNewResponse response = inclusionRatingNewService
                    .updateStatus(id, status, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating inclusion rating test status", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update inclusion rating test status: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/inclusion-rating-new/{id}")
    @Operation(summary = "Delete inclusion rating test")
    public ResponseEntity<?> deleteInclusionRatingNew(@PathVariable Long id) {
        try {
            inclusionRatingNewService.deleteInclusionRating(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Inclusion rating test deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting inclusion rating test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete inclusion rating test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== MICROSTRUCTURE TEST (NEW TWO-TABLE DESIGN) =====
    @PostMapping("/microstructure-test")
    @Operation(summary = "Save or update microstructure test")
    public ResponseEntity<?> saveOrUpdateMicrostructureTest(
            @RequestBody FinalMicrostructureTestRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            String userIdToUse = request.getCreatedBy() != null && !request.getCreatedBy().isEmpty()
                    ? request.getCreatedBy()
                    : (userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM"));
            FinalMicrostructureTestResponse response = microstructureTestService
                    .saveOrUpdateMicrostructureTest(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving microstructure test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save microstructure test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/microstructure-test/{id}")
    @Operation(summary = "Get microstructure test by ID")
    public ResponseEntity<?> getMicrostructureTestById(@PathVariable Long id) {
        try {
            var response = microstructureTestService.getMicrostructureTestById(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving microstructure test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve microstructure test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/microstructure-test/call/{callNo}")
    @Operation(summary = "Get microstructure tests by call number")
    public ResponseEntity<?> getMicrostructureTestByCallNo(@PathVariable String callNo) {
        try {
            List<FinalMicrostructureTestResponse> response = microstructureTestService
                    .getMicrostructureTestByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving microstructure tests", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve microstructure tests: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/microstructure-test/{id}/status")
    @Operation(summary = "Update microstructure test status")
    public ResponseEntity<?> updateMicrostructureTestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            String userIdToUse = userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM");
            FinalMicrostructureTestResponse response = microstructureTestService
                    .updateStatus(id, status, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating microstructure test status", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update microstructure test status: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/microstructure-test/{id}")
    @Operation(summary = "Delete microstructure test")
    public ResponseEntity<?> deleteMicrostructureTest(@PathVariable Long id) {
        try {
            microstructureTestService.deleteMicrostructureTest(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Microstructure test deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting microstructure test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete microstructure test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== FREEDOM FROM DEFECTS TEST (NEW TWO-TABLE DESIGN) =====
    @PostMapping("/freedom-from-defects-test")
    @Operation(summary = "Save or update freedom from defects test")
    public ResponseEntity<?> saveOrUpdateFreedomFromDefectsTest(
            @RequestBody FinalFreedomFromDefectsTestRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            String userIdToUse = request.getCreatedBy() != null && !request.getCreatedBy().isEmpty()
                    ? request.getCreatedBy()
                    : (userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM"));
            FinalFreedomFromDefectsTestResponse response = freedomFromDefectsTestService
                    .saveOrUpdateFreedomFromDefectsTest(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving freedom from defects test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save freedom from defects test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/freedom-from-defects-test/{id}")
    @Operation(summary = "Get freedom from defects test by ID")
    public ResponseEntity<?> getFreedomFromDefectsTestById(@PathVariable Long id) {
        try {
            var response = freedomFromDefectsTestService.getFreedomFromDefectsTestById(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving freedom from defects test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve freedom from defects test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/freedom-from-defects-test/call/{callNo}")
    @Operation(summary = "Get freedom from defects tests by call number")
    public ResponseEntity<?> getFreedomFromDefectsTestByCallNo(@PathVariable String callNo) {
        try {
            List<FinalFreedomFromDefectsTestResponse> response = freedomFromDefectsTestService
                    .getFreedomFromDefectsTestByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving freedom from defects tests", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve freedom from defects tests: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/freedom-from-defects-test/{id}/status")
    @Operation(summary = "Update freedom from defects test status")
    public ResponseEntity<?> updateFreedomFromDefectsTestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            String userIdToUse = userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM");
            FinalFreedomFromDefectsTestResponse response = freedomFromDefectsTestService
                    .updateStatus(id, status, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating freedom from defects test status", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update freedom from defects test status: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/freedom-from-defects-test/{id}")
    @Operation(summary = "Delete freedom from defects test")
    public ResponseEntity<?> deleteFreedomFromDefectsTest(@PathVariable Long id) {
        try {
            freedomFromDefectsTestService.deleteFreedomFromDefectsTest(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Freedom from defects test deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting freedom from defects test", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete freedom from defects test: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== DIMENSIONAL INSPECTION (NEW TWO-TABLE DESIGN) =====
    /**
     * Save or update dimensional inspection with samples.
     * Supports both first save (create) and subsequent saves (pause/resume).
     */
    @PostMapping("/dimensional-inspection")
    @Operation(summary = "Save or update dimensional inspection")
    public ResponseEntity<?> saveOrUpdateDimensionalInspection(
            @RequestBody FinalDimensionalInspectionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            // Priority: createdBy from request > X-User-Id header > principal > SYSTEM
            String userIdToUse = request.getCreatedBy() != null && !request.getCreatedBy().isEmpty()
                    ? request.getCreatedBy()
                    : (userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM"));
            FinalDimensionalInspectionResponse response = dimensionalInspectionNewService
                    .saveOrUpdateDimensionalInspection(request, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving dimensional inspection", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save dimensional inspection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dimensional-inspection/call/{callNo}")
    @Operation(summary = "Get dimensional inspections by call number")
    public ResponseEntity<?> getDimensionalInspectionByCallNo(@PathVariable String callNo) {
        try {
            List<FinalDimensionalInspectionResponse> response = dimensionalInspectionNewService
                    .getDimensionalInspectionsByCall(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving dimensional inspections", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve dimensional inspections: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/dimensional-inspection/{id}/status")
    @Operation(summary = "Update dimensional inspection status")
    public ResponseEntity<?> updateDimensionalInspectionStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            // Priority: X-User-Id header > principal > SYSTEM
            String userIdToUse = userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM");
            FinalDimensionalInspectionResponse response = dimensionalInspectionNewService
                    .updateStatus(id, status, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating dimensional inspection status", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update status: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/dimensional-inspection/{id}")
    @Operation(summary = "Delete dimensional inspection")
    public ResponseEntity<?> deleteDimensionalInspection(@PathVariable Long id) {
        try {
            dimensionalInspectionNewService.deleteDimensionalInspection(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Dimensional inspection deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting dimensional inspection", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete dimensional inspection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ===== DIMENSIONAL INSPECTION (FLAT STRUCTURE - FOR VISUAL INSPECTION PAGE) =====
    /**
     * Save or update dimensional inspection with flat structure.
     * Used by Final Visual Inspection page.
     * Supports UPSERT pattern for pause/resume functionality.
     */
    @PostMapping("/dimensional-inspection-flat")
    @Operation(summary = "Save or update dimensional inspection (flat structure)")
    public ResponseEntity<?> saveDimensionalInspectionFlat(
            @RequestBody FinalDimensionalInspectionFlatDto dto,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            String userIdToUse = (dto.getCreatedBy() != null && !dto.getCreatedBy().isEmpty()) 
                    ? dto.getCreatedBy() 
                    : (userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM"));
            FinalDimensionalInspectionFlat response = dimensionalInspectionFlatService
                    .saveDimensionalInspection(dto, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving dimensional inspection (flat)", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to save dimensional inspection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dimensional-inspection-flat/call/{callNo}")
    @Operation(summary = "Get dimensional inspections (flat) by call number")
    public ResponseEntity<?> getDimensionalInspectionFlatByCallNo(@PathVariable String callNo) {
        try {
            List<FinalDimensionalInspectionFlat> response = dimensionalInspectionFlatService
                    .getDimensionalInspectionByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error retrieving dimensional inspections (flat)", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve dimensional inspections: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/dimensional-inspection-flat/{id}")
    @Operation(summary = "Get dimensional inspection (flat) by ID")
    public ResponseEntity<?> getDimensionalInspectionFlatById(@PathVariable Long id) {
        try {
            var response = dimensionalInspectionFlatService.getDimensionalInspectionById(id);
            if (response.isPresent()) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response.get()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(null), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("Error retrieving dimensional inspection (flat)", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to retrieve dimensional inspection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/dimensional-inspection-flat/{id}")
    @Operation(summary = "Update dimensional inspection (flat)")
    public ResponseEntity<?> updateDimensionalInspectionFlat(
            @PathVariable Long id,
            @RequestBody FinalDimensionalInspectionFlatDto dto,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            Principal principal) {
        try {
            dto.setId(id);
            String userIdToUse = userId != null ? userId : (principal != null ? principal.getName() : "SYSTEM");
            FinalDimensionalInspectionFlat response = dimensionalInspectionFlatService
                    .updateDimensionalInspection(dto, userIdToUse);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating dimensional inspection (flat)", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to update dimensional inspection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/dimensional-inspection-flat/{id}")
    @Operation(summary = "Delete dimensional inspection (flat)")
    public ResponseEntity<?> deleteDimensionalInspectionFlat(@PathVariable Long id) {
        try {
            dimensionalInspectionFlatService.deleteDimensionalInspection(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Dimensional inspection deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting dimensional inspection (flat)", e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Failed to delete dimensional inspection: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

