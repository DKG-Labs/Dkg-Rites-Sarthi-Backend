package com.sarthi.controller;

import com.sarthi.dto.Calibration.CreateIeVendorCalibrationInspectionRequestDto;
import com.sarthi.dto.Calibration.IeVendorCalibrationInspectionResponseDto;
import com.sarthi.dto.VendorCalibrationHeaderRequestDto;
import com.sarthi.dto.VendorCalibrationHeaderResponseDto;
import com.sarthi.entity.VendorMaster;
import com.sarthi.repository.VendorMasterRepository;
import com.sarthi.service.VendorCalibrationService;
import com.sarthi.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vendor/calibration")
@CrossOrigin(origins = "*")
public class VendorCalibrationController {

    private static final Logger logger = LoggerFactory.getLogger(VendorCalibrationController.class);

    @Autowired
    private VendorCalibrationService calibrationService;

    @Autowired
    private VendorMasterRepository vendorMasterRepository;

    /**
     * Create or update a calibration group (header + details)
     * POST /api/vendor/calibration
     */
    @PostMapping
    public ResponseEntity<Object> createOrUpdateCalibration(
            @RequestBody VendorCalibrationHeaderRequestDto requestDto,
            @RequestParam(required = false, defaultValue = "vendor") String userId,
            Principal principal) {
        logger.info("Received request to save/update calibration group for vendor: {}", requestDto.getVendorCode());
        try {
            String finalUserId = userId;
            if (principal != null && principal.getName() != null && !principal.getName().isEmpty()) {
                finalUserId = principal.getName();
            } else if (finalUserId == null || "vendor".equals(finalUserId) || finalUserId.trim().isEmpty()) {
                finalUserId = requestDto.getVendorCode();
            }
            VendorCalibrationHeaderResponseDto response = calibrationService.createOrUpdateCalibrationGroup(requestDto, finalUserId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error saving calibration group: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all calibration groups for a vendor
     * GET /api/vendor/calibration/vendor/{vendorCode}
     */
    @GetMapping("/vendor/{vendorCode}")
    public ResponseEntity<Object> getCalibrationsByVendor(@PathVariable String vendorCode) {
        logger.info("Received request to fetch calibration groups for vendor: {}", vendorCode);
        try {
            List<VendorCalibrationHeaderResponseDto> calibrations = calibrationService.getCalibrationsByVendor(vendorCode);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(calibrations), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching vendor calibrations: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a calibration group by ID
     * GET /api/vendor/calibration/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<Object> getCalibrationGroupById(@PathVariable Long id) {
        logger.info("Received request to fetch calibration group by ID: {}", id);
        try {
            VendorCalibrationHeaderResponseDto response = calibrationService.getCalibrationGroupById(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching calibration group detail: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a calibration group (parent + details cascaded)
     * DELETE /api/vendor/calibration/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCalibrationGroup(@PathVariable Long id) {
        logger.info("Received request to delete calibration group with ID: {}", id);
        try {
            calibrationService.deleteCalibrationGroup(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Calibration group deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting calibration group: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete an individual calibration detail record
     * DELETE /api/vendor/calibration/detail/{detailId}
     */
    @DeleteMapping("/detail/{detailId}")
    public ResponseEntity<Object> deleteCalibrationDetail(@PathVariable Long detailId) {
        logger.info("Received request to delete calibration detail with ID: {}", detailId);
        try {
            calibrationService.deleteCalibrationDetail(detailId);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse("Calibration detail record deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error deleting calibration detail: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/allCalibrations/{vendorCode}")
    public ResponseEntity<Object> getByVendorCode(@PathVariable String vendorCode) {

        List<VendorCalibrationHeaderResponseDto> res = calibrationService.getByVendorCode(vendorCode);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);

    }

    @PostMapping("/ie-calibration-inspection")
    public ResponseEntity<Object> createInspection(
            @RequestBody CreateIeVendorCalibrationInspectionRequestDto requestDto) {

        IeVendorCalibrationInspectionResponseDto res =  calibrationService.createInspection(requestDto);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);

    }

    @GetMapping("/ie-calibration-inspection/{callNo}")
    public ResponseEntity<Object> getInspectionByCallNo(@PathVariable String callNo) {
        logger.info("Received request to fetch IE calibration inspection for call: {}", callNo);
        try {
            IeVendorCalibrationInspectionResponseDto response = calibrationService.getInspectionByCallNo(callNo);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching IE calibration inspection: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Lookup vendor code by manufacturer/vendor name.
     * GET /api/vendor/calibration/vendor-code-by-name?vendorName=...
     *
     * Returns the vendor_code from vendor_master matching the given vendor name.
     */
    @GetMapping("/vendor-code-by-name")
    public ResponseEntity<Object> getVendorCodeByName(@RequestParam String vendorName) {
        logger.info("Looking up vendor code for name: {}", vendorName);
        try {
            // Try exact match first
            Optional<VendorMaster> exact = vendorMasterRepository.findByVendorNameIgnoreCase(vendorName);
            if (exact.isPresent()) {
                return new ResponseEntity<>(
                        ResponseBuilder.getSuccessResponse(
                                Map.of("vendorCode", exact.get().getVendorCode(),
                                       "vendorName", exact.get().getVendorName())),
                        HttpStatus.OK);
            }

            // Fall back to partial match
            List<VendorMaster> partialMatches = vendorMasterRepository.findByVendorNameContainingIgnoreCase(vendorName);
            if (!partialMatches.isEmpty()) {
                VendorMaster best = partialMatches.get(0);
                return new ResponseEntity<>(
                        ResponseBuilder.getSuccessResponse(
                                Map.of("vendorCode", best.getVendorCode(),
                                       "vendorName", best.getVendorName())),
                        HttpStatus.OK);
            }

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error looking up vendor code by name: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

