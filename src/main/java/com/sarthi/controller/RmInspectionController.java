package com.sarthi.controller;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.RmFinishInspectionDto;
import com.sarthi.dto.RmPreInspectionDataDto;
import com.sarthi.dto.RmHeatFinalResultDto;
import com.sarthi.dto.RmLadleValuesDto;
import com.sarthi.entity.RmVisualInspection;
import com.sarthi.repository.RmVisualInspectionRepository;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.service.RmInspectionService;
import com.sarthi.service.JwtService;
import com.sarthi.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.security.Principal;

/**
 * REST Controller for Raw Material inspection operations.
 * JWT protected endpoints for IE to save inspection results.
 */
@RestController
@RequestMapping("/api/rm-inspection")
@CrossOrigin(origins = "*")
public class RmInspectionController {

    private static final Logger logger = LoggerFactory.getLogger(RmInspectionController.class);

    @Autowired
    private RmInspectionService service;

    @Autowired
    private RmVisualInspectionRepository visualRepository;

    /**
     * Finish Raw Material inspection - saves all submodule data.
     * Called when inspector clicks "Finish Inspection" button.
     */
    @PostMapping("/finish")
    public ResponseEntity<Object> finishInspection(
            @RequestBody RmFinishInspectionDto dto,
            Principal principal) {

        // Use createdBy from payload if provided, otherwise use principal or fallback to "system"
        String userId = (dto.getCreatedBy() != null && !dto.getCreatedBy().isEmpty()) ?
                        dto.getCreatedBy() :
                        (principal != null ? principal.getName() : "system");

        logger.info("POST /api/rm-inspection/finish - Finishing inspection for call: {} by user: {}",
            dto.getInspectionCallNo(), userId);

        try {
            String result = service.finishInspection(dto, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Validation errors - return BAD_REQUEST with clear message
            logger.warn("Validation error for RM inspection: {}", e.getMessage());
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.USER_INVALID_INPUT,
                AppConstant.ERROR_TYPE_CODE_VALIDATION,
                AppConstant.ERROR_TYPE_VALIDATION,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("Error finishing RM inspection: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Pause Raw Material inspection - saves all submodule data WITHOUT changing status.
     * Called when inspector clicks "Pause Inspection" button.
     * POST /api/rm-inspection/pause
     */
    @PostMapping("/pause")
    public ResponseEntity<Object> pauseInspection(
            @RequestBody RmFinishInspectionDto dto,
            Principal principal) {

        // Use createdBy from payload if provided, otherwise use principal or fallback to "system"
        String userId = (dto.getCreatedBy() != null && !dto.getCreatedBy().isEmpty()) ?
                        dto.getCreatedBy() :
                        (principal != null ? principal.getName() : "system");

        logger.info("POST /api/rm-inspection/pause - Pausing inspection for call: {} by user: {}",
            dto.getInspectionCallNo(), userId);

        try {
            String result = service.pauseInspection(dto, userId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(result), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error pausing RM inspection: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get complete inspection data by call number.
     */
    @GetMapping("/call/{callNo}")
    public ResponseEntity<Object> getByCallNo(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/call/{}", callNo);
        try {
            RmFinishInspectionDto dto = service.getByCallNo(callNo);
            if (dto == null) {
                ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Inspection not found"
                );
                return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching RM inspection: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get cumulative summary data only (pre-inspection data).
     */
    @GetMapping("/summary/{callNo}")
    public ResponseEntity<Object> getSummaryByCallNo(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/summary/{}", callNo);
        try {
            RmPreInspectionDataDto dto = service.getSummaryByCallNo(callNo);
            if (dto == null) {
                ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Summary data not found"
                );
                return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching summary data: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get final inspection results for all heats.
     */
    @GetMapping("/final-results/{callNo}")
    public ResponseEntity<Object> getFinalResultsByCallNo(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/final-results/{}", callNo);
        try {
            List<RmHeatFinalResultDto> results = service.getFinalResultsByCallNo(callNo);
            if (results == null || results.isEmpty()) {
                ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Final results not found"
                );
                return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(results), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching final results: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get ladle values (chemical analysis from vendor) for all heats.
     * Used in Material Testing page to display ladle values.
     */
    @GetMapping("/ladle-values/{callNo}")
    public ResponseEntity<Object> getLadleValuesByCallNo(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/ladle-values/{}", callNo);
        try {
            List<RmLadleValuesDto> ladleValues = service.getLadleValuesByCallNo(callNo);
            if (ladleValues == null || ladleValues.isEmpty()) {
                ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_RESOURCE,
                    "Ladle values not found"
                );
                return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(ladleValues), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching ladle values: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get Visual Inspection data for all heats in a call.
     * GET /api/rm-inspection/visual/{callNo}
     * Returns all visual inspection records (both passed and draft) converted to DTO format
     */
    @GetMapping("/visual/{callNo}")
    public ResponseEntity<Object> getVisualInspection(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/visual/{}", callNo);
        try {
            // Fetch visual inspection data from backend
            List<RmVisualInspection> visualData = visualRepository.findByInspectionCallNo(callNo);

            // Convert entities to DTOs with defects and defectLengths maps
            List<Object> visualDtos = new ArrayList<>();
            for (RmVisualInspection entity : visualData) {
                Map<String, Object> dto = new HashMap<>();
                dto.put("inspectionCallNo", entity.getInspectionCallNo());
                dto.put("heatNo", entity.getHeatNo());
                dto.put("heatIndex", entity.getHeatIndex());

                // Convert entity defect booleans to map
                Map<String, Boolean> defects = new HashMap<>();
                defects.put("No Defect", entity.getNoDefect());
                defects.put("Distortion", entity.getDistortion());
                defects.put("Twist", entity.getTwist());
                defects.put("Kink", entity.getKink());
                defects.put("Not Straight", entity.getNotStraight());
                defects.put("Fold", entity.getFold());
                defects.put("Lap", entity.getLap());
                defects.put("Crack", entity.getCrack());
                defects.put("Pit", entity.getPit());
                defects.put("Groove", entity.getGroove());
                defects.put("Excessive Scaling", entity.getExcessiveScaling());
                defects.put("Internal Defect (Piping, Segregation)", entity.getInternalDefect());
                defects.put("Other", entity.getOther());
                dto.put("defects", defects);

                // Convert entity defect lengths to map
                Map<String, BigDecimal> defectLengths = new HashMap<>();
                if (entity.getDistortionLength() != null) defectLengths.put("Distortion", entity.getDistortionLength());
                if (entity.getTwistLength() != null) defectLengths.put("Twist", entity.getTwistLength());
                if (entity.getKinkLength() != null) defectLengths.put("Kink", entity.getKinkLength());
                if (entity.getNotStraightLength() != null) defectLengths.put("Not Straight", entity.getNotStraightLength());
                if (entity.getFoldLength() != null) defectLengths.put("Fold", entity.getFoldLength());
                if (entity.getLapLength() != null) defectLengths.put("Lap", entity.getLapLength());
                if (entity.getCrackLength() != null) defectLengths.put("Crack", entity.getCrackLength());
                if (entity.getPitLength() != null) defectLengths.put("Pit", entity.getPitLength());
                if (entity.getGrooveLength() != null) defectLengths.put("Groove", entity.getGrooveLength());
                if (entity.getExcessiveScalingLength() != null) defectLengths.put("Excessive Scaling", entity.getExcessiveScalingLength());
                if (entity.getInternalDefectLength() != null) defectLengths.put("Internal Defect (Piping, Segregation)", entity.getInternalDefectLength());
                if (entity.getOtherLength() != null) defectLengths.put("Other", entity.getOtherLength());
                dto.put("defectLengths", defectLengths);

                if (entity.getOtherRemarks() != null) {
                    dto.put("otherRemarks", entity.getOtherRemarks());
                }

                // Add passedAt if available
                if (entity.getPassedAt() != null) {
                    dto.put("passedAt", entity.getPassedAt());
                }

                if (entity.getDefectCount() != null) {
                    dto.put("defectCount", entity.getDefectCount());
                }

                if (entity.getWeightRejected() != null) {
                    dto.put("weightRejected", entity.getWeightRejected());
                }

                visualDtos.add(dto);
            }

            logger.info("Fetched and converted {} visual inspection records for call: {}", visualDtos.size(), callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(visualDtos), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching visual inspection data: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Save Visual Inspection Pass status for a heat.
     * POST /api/rm-inspection/visual/pass
     * Creates or updates a single record per heat with all defect selections.
     */
    @PostMapping("/visual/pass")
    public ResponseEntity<Object> saveVisualInspectionPass(@RequestBody Map<String, Object> passData) {
        logger.info("POST /api/rm-inspection/visual/pass - Marking heat as passed");
        try {
            String callNo = (String) passData.get("inspectionCallNo");
            String heatNo = (String) passData.get("heatNo");
            Integer heatIndex = (Integer) passData.get("heatIndex");
            String createdBy = (String) passData.get("createdBy");

            @SuppressWarnings("unchecked")
            List<String> selectedDefects = (List<String>) passData.get("selectedDefects");

            @SuppressWarnings("unchecked")
            Map<String, Object> defectLengths = (Map<String, Object>) passData.get("defectLengths");

            Integer defectCount = passData.get("defectCount") != null ? 
                ((Number) passData.get("defectCount")).intValue() : null;
                
            BigDecimal weightRejected = passData.get("weightRejected") != null ? 
                new BigDecimal(passData.get("weightRejected").toString()) : null;

            if (callNo == null || heatNo == null || selectedDefects == null || selectedDefects.isEmpty()) {
                ErrorDetails errorDetails = new ErrorDetails(
                    400,
                    AppConstant.ERROR_TYPE_CODE_INVALID,
                    AppConstant.ERROR_TYPE_INVALID,
                    "Missing required fields: inspectionCallNo, heatNo, or selectedDefects"
                );
                return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
            }

            // Find or create single record per heat
            List<RmVisualInspection> existingRecords = visualRepository.findByInspectionCallNoAndHeatNo(callNo, heatNo);
            RmVisualInspection record = existingRecords.stream()
                .findFirst()
                .orElse(null);

            LocalDateTime now = LocalDateTime.now();

            if (record == null) {
                // Create new record
                record = new RmVisualInspection();
                record.setInspectionCallNo(callNo);
                record.setHeatNo(heatNo);
                record.setHeatIndex(heatIndex);
                record.setCreatedAt(now);
                logger.info("Creating new visual inspection record for call: {} heat: {}", callNo, heatNo);
            } else {
                // Update existing record
                logger.info("Updating existing visual inspection record for call: {} heat: {}", callNo, heatNo);
            }

            // Set all selected defects
            for (String defectName : selectedDefects) {
                setDefectSelection(record, defectName, true);

                // Set defect length if provided
                if (defectLengths != null && defectLengths.containsKey(defectName)) {
                    Object lengthValue = defectLengths.get(defectName);
                    if (lengthValue != null && !lengthValue.toString().isEmpty()) {
                        try {
                            BigDecimal length = new BigDecimal(lengthValue.toString());
                            setDefectLength(record, defectName, length);
                            logger.debug("Set defect length for {} to {}", defectName, length);
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid defect length value for {}: {}", defectName, lengthValue);
                        }
                    }
                }
            }

            // Set other remarks if provided
            if (passData.containsKey("otherRemarks")) {
                record.setOtherRemarks((String) passData.get("otherRemarks"));
            }

            // Set explicit calculated values
            if (defectCount != null) {
                record.setDefectCount(defectCount);
            }
            if (weightRejected != null) {
                record.setWeightRejected(weightRejected);
            }

            // Always update passed_at and createdBy on pass
            record.setPassedAt(now);
            record.setCreatedBy(createdBy);

            RmVisualInspection saved = visualRepository.save(record);
            logger.info("Successfully saved visual inspection record for heat {}", heatNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(saved), HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error saving visual inspection pass status: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get Visual Inspection Pass status for all heats in a call.
     * GET /api/rm-inspection/visual/pass/{callNo}
     * Returns all visual inspection records with passed_at timestamp
     */
    @GetMapping("/visual/pass/{callNo}")
    public ResponseEntity<Object> getVisualInspectionPass(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/visual/pass/{}", callNo);
        try {
            List<RmVisualInspection> passData = visualRepository.findByInspectionCallNo(callNo)
                .stream()
                .filter(v -> v.getPassedAt() != null)
                .collect(Collectors.toList());
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(passData), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching visual inspection pass status: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get Dimensional Check data for all heats in a call.
     * GET /api/rm-inspection/dimensional/{callNo}
     * Returns all dimensional check records with 20 sample diameters per heat
     */
    @GetMapping("/dimensional/{callNo}")
    public ResponseEntity<Object> getDimensionalCheck(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/dimensional/{}", callNo);
        try {
            // Fetch dimensional check data from service layer
            // The service method getByCallNo already includes dimensional check data
            RmFinishInspectionDto dto = service.getByCallNo(callNo);
            if (dto == null || dto.getDimensionalCheckData() == null) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(new ArrayList<>()), HttpStatus.OK);
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto.getDimensionalCheckData()), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching dimensional check data: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get Material Testing data for all heats in a call.
     * GET /api/rm-inspection/material-testing/{callNo}
     * Returns all material testing records
     */
    @GetMapping("/material-testing/{callNo}")
    public ResponseEntity<Object> getMaterialTesting(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/material-testing/{}", callNo);
        try {
            RmFinishInspectionDto dto = service.getByCallNo(callNo);
            if (dto == null || dto.getMaterialTestingData() == null) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(new ArrayList<>()), HttpStatus.OK);
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto.getMaterialTestingData()), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching material testing data: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get Packing & Storage data for a call.
     * GET /api/rm-inspection/packing-storage/{callNo}
     * Returns packing and storage inspection records (list)
     */
    @GetMapping("/packing-storage/{callNo}")
    public ResponseEntity<Object> getPackingStorage(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/packing-storage/{}", callNo);
        try {
            RmFinishInspectionDto dto = service.getByCallNo(callNo);
            if (dto == null || dto.getPackingStorageData() == null) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(new ArrayList<>()), HttpStatus.OK);
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto.getPackingStorageData()), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching packing storage data: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get Calibration Documents data for a call.
     * GET /api/rm-inspection/calibration/{callNo}
     * Returns calibration documents records (list)
     */
    @GetMapping("/calibration/{callNo}")
    public ResponseEntity<Object> getCalibrationDocuments(@PathVariable String callNo) {
        logger.info("GET /api/rm-inspection/calibration/{}", callNo);
        try {
            RmFinishInspectionDto dto = service.getByCallNo(callNo);
            if (dto == null || dto.getCalibrationDocumentsData() == null) {
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(new ArrayList<>()), HttpStatus.OK);
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(dto.getCalibrationDocumentsData()), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching calibration documents data: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                AppConstant.INTER_SERVER_ERROR,
                AppConstant.ERROR_TYPE_CODE_INTERNAL,
                AppConstant.ERROR_TYPE_ERROR,
                e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void setDefectSelection(RmVisualInspection entity, String defectName, Boolean isSelected) {
        boolean selected = isSelected != null && isSelected;
        switch (defectName.toLowerCase()) {
            case "no defect":
                entity.setNoDefect(selected);
                break;
            case "distortion":
                entity.setDistortion(selected);
                break;
            case "twist":
                entity.setTwist(selected);
                break;
            case "kink":
                entity.setKink(selected);
                break;
            case "not straight":
                entity.setNotStraight(selected);
                break;
            case "fold":
                entity.setFold(selected);
                break;
            case "lap":
                entity.setLap(selected);
                break;
            case "crack":
                entity.setCrack(selected);
                break;
            case "pit":
                entity.setPit(selected);
                break;
            case "groove":
                entity.setGroove(selected);
                break;
            case "excessive scaling":
                entity.setExcessiveScaling(selected);
                break;
            case "internal defect (piping, segregation)":
            case "internal defect":
                entity.setInternalDefect(selected);
                break;
            case "other":
                entity.setOther(selected);
                break;
        }
    }

    private void setDefectLength(RmVisualInspection entity, String defectName, BigDecimal length) {
        switch (defectName.toLowerCase()) {
            case "distortion":
                entity.setDistortionLength(length);
                break;
            case "twist":
                entity.setTwistLength(length);
                break;
            case "kink":
                entity.setKinkLength(length);
                break;
            case "not straight":
                entity.setNotStraightLength(length);
                break;
            case "fold":
                entity.setFoldLength(length);
                break;
            case "lap":
                entity.setLapLength(length);
                break;
            case "crack":
                entity.setCrackLength(length);
                break;
            case "pit":
                entity.setPitLength(length);
                break;
            case "groove":
                entity.setGrooveLength(length);
                break;
            case "excessive scaling":
                entity.setExcessiveScalingLength(length);
                break;
            case "internal defect (piping, segregation)":
            case "internal defect":
                entity.setInternalDefectLength(length);
                break;
            case "other":
                entity.setOtherLength(length);
                break;
        }
    }

}

