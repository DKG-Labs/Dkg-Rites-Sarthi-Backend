package com.sarthi.controller;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.AvailableHeatNumberDto;
import com.sarthi.dto.LoginRequestDto;
import com.sarthi.dto.LoginResponseDto;
import com.sarthi.dto.vendorDtos.PoResponseDto;
import com.sarthi.dto.vendorDtos.VendorPoHeaderResponseDto;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.service.VendorHeatNumberService;
import com.sarthi.service.VendorPoService;
import com.sarthi.service.vendorService;
import com.sarthi.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor")
@CrossOrigin(origins = "*")
public class VendorController {

    private static final Logger logger = LoggerFactory.getLogger(VendorController.class);

    @Autowired
    private VendorPoService vService;

    @Autowired
    private VendorHeatNumberService vendorHeatNumberService;

    @GetMapping("/poData")
    // public ResponseEntity<Object> login(@RequestParam String vendorCode) {
    //     List<VendorPoHeaderResponseDto> res = vService.getPoListByVendorCode(vendorCode);
    //     return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    // }
    public ResponseEntity<Object> getPoData(@RequestParam String vendorCode) {
        List<VendorPoHeaderResponseDto> res = vService.getPoListByVendorCode(vendorCode);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
/*
    @GetMapping("/po-assigned")
    public ResponseEntity<Object> getPoAssigned(@RequestParam(required = false) String vendorId, @RequestParam String vendorType) {
        // If vendorId is not provided, return empty list
        if (vendorId == null || vendorId.isEmpty()) {
            return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(List.of()), HttpStatus.OK);
        }
        List<VendorPoHeaderResponseDto> res = vService.getPoListByVendorCode(vendorId, vendorType);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
*/

    @GetMapping("/po-assigned")
    public ResponseEntity<Object> getPoAssigned(@RequestParam(required = false) String vendorId) {
        // If vendorId is not provided, return empty list
        if (vendorId == null || vendorId.isEmpty()) {
            return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(List.of()), HttpStatus.OK);
        }
        List<VendorPoHeaderResponseDto> res = vService.getPoListByVendorCode(vendorId);
        return new ResponseEntity<Object>(ResponseBuilder.getSuccessResponse(res), HttpStatus.OK);
    }
    /**
     * Get available heat numbers for a vendor.
     *
     * This endpoint:
     * 1. Fetches all heat numbers from database (including exhausted ones)
     * 2. Filters and returns only available heat numbers (isAvailable = true)
     * 3. Exhausted heat numbers remain in database but are not returned
     *
     * Used for: Vendor dropdown in "Raw Material Raising Call" section
     *
     * Availability Criteria:
     * - Remaining quantity > 0
     * - Inventory status != EXHAUSTED
     *
     * Note: Heat numbers with UNDER_INSPECTION, ACCEPTED, or REJECTED status
     * are still available for selection, allowing vendors to raise inspection
     * calls for these materials. Only EXHAUSTED entries are filtered out.
     *
     * GET /api/vendor/available-heat-numbers/{vendorCode}
     *
     * @param vendorCode The vendor code
     * @return List of available heat numbers only
     */
    @GetMapping("/available-heat-numbers/{vendorCode}")
    public ResponseEntity<Object> getAvailableHeatNumbers(@PathVariable String vendorCode) {
        logger.info("Received request to fetch available heat numbers for vendor: {}", vendorCode);

        try {
            List<AvailableHeatNumberDto> availableHeatNumbers = vendorHeatNumberService.getAvailableHeatNumbers(vendorCode);
            logger.info("Found {} available heat numbers for vendor: {}", availableHeatNumbers.size(), vendorCode);

            if (availableHeatNumbers.isEmpty()) {
                logger.warn("No available heat numbers found for vendor: {} (exhausted heat numbers remain in database)",
                           vendorCode);
            }

            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(availableHeatNumbers), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching available heat numbers for vendor: {}", vendorCode, e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.INTER_SERVER_ERROR,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    "Failed to fetch available heat numbers: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all heat numbers for a vendor (including exhausted ones).
     *
     * This endpoint:
     * 1. Fetches all heat numbers from database
     * 2. Returns complete list with availability status marked
     * 3. Does NOT filter out exhausted heat numbers
     *
     * Each heat number in the response has isAvailable field:
     * - true: Available for new inspection calls
     * - false: Exhausted or already allocated
     *
     * Used for:
     * - Reporting and analytics
     * - Historical data viewing
     * - Audit trail and compliance
     * - Inventory management dashboards
     *
     * GET /api/vendor/all-heat-numbers/{vendorCode}
     *
     * @param vendorCode The vendor code
     * @return List of all heat numbers (available + exhausted) with availability status
     */
    @GetMapping("/all-heat-numbers/{vendorCode}")
    public ResponseEntity<Object> getAllHeatNumbers(@PathVariable String vendorCode) {
        logger.info("Received request to fetch all heat numbers (including exhausted) for vendor: {}", vendorCode);

        try {
            List<AvailableHeatNumberDto> allHeatNumbers = vendorHeatNumberService.getAllHeatNumbers(vendorCode);

            long availableCount = allHeatNumbers.stream()
                    .filter(AvailableHeatNumberDto::isAvailable)
                    .count();
            long exhaustedCount = allHeatNumbers.size() - availableCount;

            logger.info("Found {} total heat numbers for vendor: {} ({} available, {} exhausted)",
                       allHeatNumbers.size(), vendorCode, availableCount, exhaustedCount);

            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(allHeatNumbers), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching all heat numbers for vendor: {}", vendorCode, e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.INTER_SERVER_ERROR,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    "Failed to fetch heat numbers: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Check if a specific heat number has available inventory.
     * GET /api/vendor/check-heat-availability
     */
    @GetMapping("/check-heat-availability")
    public ResponseEntity<Object> checkHeatAvailability(
            @RequestParam String heatNumber,
            @RequestParam String tcNumber) {
        logger.info("Checking availability for heat number: {}, TC number: {}", heatNumber, tcNumber);

        try {
            boolean isAvailable = vendorHeatNumberService.isHeatNumberAvailable(heatNumber, tcNumber);

            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(
                    new HeatAvailabilityResponse(heatNumber, tcNumber, isAvailable)),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error checking heat availability for heat: {}, TC: {}", heatNumber, tcNumber, e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.INTER_SERVER_ERROR,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    "Failed to check heat availability: " + e.getMessage()
            );
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Inner class for heat availability response
     */
    @SuppressWarnings("unused")
    private static class HeatAvailabilityResponse {
        public String heatNumber;
        public String tcNumber;
        public boolean isAvailable;

        public HeatAvailabilityResponse(String heatNumber, String tcNumber, boolean isAvailable) {
            this.heatNumber = heatNumber;
            this.tcNumber = tcNumber;
            this.isAvailable = isAvailable;
        }
    }

}
