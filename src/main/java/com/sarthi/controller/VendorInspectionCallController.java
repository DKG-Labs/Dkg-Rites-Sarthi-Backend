package com.sarthi.controller;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.VendorInspectionCallStatusDto;
import com.sarthi.service.VendorInspectionCallService;
import com.sarthi.util.ResponseBuilder;
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
 * Controller for Vendor Inspection Call operations.
 * Provides endpoints for vendors to view their inspection calls with workflow status.
 */
@RestController
@RequestMapping("/api/vendor/inspection-calls")
@CrossOrigin(origins = "*")
@Tag(name = "Vendor Inspection Calls", description = "APIs for vendor inspection call management")
public class VendorInspectionCallController {

    private static final Logger logger = LoggerFactory.getLogger(VendorInspectionCallController.class);

    @Autowired
    private VendorInspectionCallService vendorInspectionCallService;

    /**
     * Get all inspection calls for a vendor with workflow status.
     * GET /api/vendor/inspection-calls/status?vendorId={vendorId}
     * 
     * @param vendorId Vendor ID to filter inspection calls
     * @return List of inspection calls with workflow status
     */
    @GetMapping("/status")
    @Operation(
        summary = "Get vendor inspection calls with workflow status",
        description = "Fetches all inspection calls for a vendor with their latest workflow transition status"
    )
    public ResponseEntity<Object> getVendorInspectionCallsWithStatus(
            @RequestParam String vendorId) {
        
        logger.info("Received request to fetch inspection calls with status for vendor: {}", vendorId);
        
        try {
            List<VendorInspectionCallStatusDto> calls = 
                vendorInspectionCallService.getVendorInspectionCallsWithStatus(vendorId);
            
            logger.info("Successfully fetched {} inspection calls for vendor: {}", calls.size(), vendorId);
            
            return new ResponseEntity<>(
                ResponseBuilder.getSuccessResponse(calls), 
                HttpStatus.OK
            );
        } catch (Exception e) {
            logger.error("Error fetching inspection calls for vendor: {}", vendorId, e);
            return new ResponseEntity<>(
                ResponseBuilder.getErrorResponse(
                    new com.sarthi.exception.ErrorDetails(
                        AppConstant.INTER_SERVER_ERROR,
                        AppConstant.ERROR_TYPE_CODE_INTERNAL,
                        AppConstant.ERROR_TYPE_ERROR,
                        "Error fetching inspection calls: " + e.getMessage()
                    )
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    /**
     * Get merged TC documents for a specific call number.
     * GET /api/vendor/inspection-calls/tc-docs/{callNo}
     *
     * @param callNo Call number to fetch TC documents for
     * @return PDF byte array
     */
    @GetMapping("/tc-docs/{callNo}")
    @Operation(
        summary = "Get merged TC documents for a call",
        description = "Fetches and merges all TC documents associated with the provided call number"
    )
    public ResponseEntity<byte[]> getTcDocsByCallNo(@PathVariable String callNo) {
        logger.info("Received request to fetch TC documents for call: {}", callNo);
        
        try {
            byte[] pdfBytes = vendorInspectionCallService.getTcDocsByCallNo(callNo);
            
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"TC_Documents_" + callNo + ".pdf\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);
        } catch (com.sarthi.exception.BusinessException e) {
            logger.warn("Business exception while fetching TC docs for call {}: {}", callNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error fetching TC documents for call: {}", callNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

