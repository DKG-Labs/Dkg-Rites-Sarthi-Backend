package com.sarthi.controller;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.InventoryEntryRequestDto;
import com.sarthi.dto.InventoryBulkEntryRequestDto;
import com.sarthi.dto.InventoryEntryResponseDto;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.InventoryEntryRepository;
import com.sarthi.service.AzureBlobStorageService;
import com.sarthi.service.InventoryEntryService;
import com.sarthi.util.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for inventory entry operations
 */
@RestController
@RequestMapping("/api/vendor/inventory")
@CrossOrigin(origins = "*")
public class InventoryEntryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryEntryController.class);

    @Autowired
    private InventoryEntryService inventoryEntryService;

    @Autowired
    private AzureBlobStorageService azureBlobStorageService;

    @Autowired
    private InventoryEntryRepository inventoryEntryRepository;

    /**
     * View the TC file (PDF) for a given TC number — streams from Azure Blob
     * GET /api/vendor/inventory/tc-file?tcNumber=xxx&vendorCode=xxx
     */
    @GetMapping("/tc-file")
    public ResponseEntity<?> viewTcFile(
            @RequestParam String tcNumber,
            @RequestParam String vendorCode) {
        logger.info("Request to view TC file for tcNumber={}, vendor={}", tcNumber, vendorCode);
        try {
            // Find any inventory entry for this TC number to get the stored blob URL
            var entries = inventoryEntryRepository.findByTcNumberAndVendorCode(tcNumber, vendorCode);
            if (entries == null || entries.isEmpty()) {
                return ResponseEntity.status(404).body("No inventory entry found for TC: " + tcNumber);
            }
            String tcFilePath = entries.get(0).getTcFilePath();
            if (tcFilePath == null || tcFilePath.isBlank()) {
                return ResponseEntity.status(404).body("No TC file uploaded for TC: " + tcNumber);
            }

            // Extract blob name from the Azure URL (last path segment)
            String blobName = tcFilePath.substring(tcFilePath.lastIndexOf('/') + 1);
            
            // URL decode the blob name to handle URL encoded characters like %3A (for :)
            blobName = java.net.URLDecoder.decode(blobName, java.nio.charset.StandardCharsets.UTF_8);

            byte[] pdfBytes = azureBlobStorageService.downloadFile(blobName);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + blobName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);

        } catch (Exception e) {
            logger.error("Error fetching TC file: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch TC file: " + e.getMessage());
        }
    }

    /**
     * Create a new inventory entry
     * POST /api/vendor/inventory/entries
     */
    @PostMapping("/entries")
    public ResponseEntity<Object> createInventoryEntry(@RequestBody InventoryEntryRequestDto requestDto) {
        logger.info("Received request to create inventory entry for vendor: {}", requestDto.getVendorCode());

        try {
            InventoryEntryResponseDto response = inventoryEntryService.createInventoryEntry(requestDto);
            logger.info("Inventory entry created successfully with ID: {}", response.getId());
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error creating inventory entry: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create multiple inventory entries with multiple heats against one TC
     * POST /api/vendor/inventory/bulk-entries
     */
    @PostMapping("/bulk-entries")
    public ResponseEntity<Object> createMultipleInventoryEntries(@RequestBody InventoryBulkEntryRequestDto requestDto) {
        logger.info("Received request to create bulk inventory entries for vendor: {} and TC: {}",
                requestDto.getVendorCode(), requestDto.getTcNumber());

        try {
            // Check TC uniqueness again before processing
            if (inventoryEntryService.existsByTcNumber(requestDto.getTcNumber(), requestDto.getVendorCode())) {
                ErrorDetails errorDetails = new ErrorDetails(
                        AppConstant.ERROR_CODE_INVALID,
                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                        AppConstant.ERROR_TYPE_VALIDATION,
                        "This TC Number already exists in your inventory.");
                return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails), HttpStatus.BAD_REQUEST);
            }

            List<InventoryEntryResponseDto> response = inventoryEntryService.createMultipleInventoryEntries(requestDto,
                    null);
            logger.info("Created {} inventory entries successfully", response.size());
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(response), HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error creating bulk inventory entries: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    "Internal Server Error: " + e.getMessage());
            return new ResponseEntity<>(ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all inventory entries for a vendor
     * GET /api/vendor/inventory/entries/{vendorCode}
     */
    @GetMapping("/entries/{vendorCode}")
    public ResponseEntity<Object> getInventoryEntries(@PathVariable String vendorCode) {
        logger.info("Received request to fetch inventory entries for vendor: {}", vendorCode);

        try {
            List<InventoryEntryResponseDto> entries = inventoryEntryService.getInventoryEntriesByVendor(vendorCode);
            logger.info("Found {} inventory entries for vendor: {}", entries.size(), vendorCode);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(entries), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error fetching inventory entries: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get inventory entry by ID
     * GET /api/vendor/inventory/entries/detail/{id}
     */
    @GetMapping("/entries/detail/{id}")
    public ResponseEntity<Object> getInventoryEntryById(@PathVariable Long id) {
        logger.info("Received request to fetch inventory entry by ID: {}", id);

        try {
            InventoryEntryResponseDto entry = inventoryEntryService.getInventoryEntryById(id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(entry), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error fetching inventory entry: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Update inventory entry status
     * PUT /api/vendor/inventory/entries/{id}/status
     */
    @PutMapping("/entries/{id}/status")
    public ResponseEntity<Object> updateInventoryStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        logger.info("Received request to update inventory entry {} status to: {}", id, status);

        try {
            InventoryEntryResponseDto entry = inventoryEntryService.updateInventoryStatus(id, status);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(entry), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error updating inventory status: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get inventory entry by heat number and TC number combination
     * GET /api/vendor/inventory/entries/heat/{heatNumber}/tc/{tcNumber}
     */
    @GetMapping("/entries/heat/{heatNumber}/tc/{tcNumber}")
    public ResponseEntity<Object> getInventoryEntryByHeatAndTc(
            @PathVariable String heatNumber,
            @PathVariable String tcNumber) {
        logger.info("Received request to fetch inventory entry by heat: {} and TC: {}", heatNumber, tcNumber);

        try {
            InventoryEntryResponseDto entry = inventoryEntryService.getInventoryEntryByHeatAndTc(heatNumber, tcNumber);
            if (entry == null) {
                logger.warn("No inventory entry found for heat: {} and TC: {}", heatNumber, tcNumber);
                return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(entry), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error fetching inventory entry: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(null),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing inventory entry
     * PUT /api/vendor/inventory/entries/{id}
     *
     * Note: Only entries with status = FRESH_PO can be updated
     */
    @PutMapping("/entries/{id}")
    public ResponseEntity<Object> updateInventoryEntry(
            @PathVariable Long id,
            @RequestBody InventoryEntryRequestDto requestDto) {
        logger.info("Received request to update inventory entry with ID: {}", id);

        try {
            InventoryEntryResponseDto updatedEntry = inventoryEntryService.updateInventoryEntry(id, requestDto);
            logger.info("Inventory entry updated successfully with ID: {}", id);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(updatedEntry), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error updating inventory entry: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_INVALID,
                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                    AppConstant.ERROR_TYPE_VALIDATION,
                    e.getMessage());
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Delete an inventory entry
     * DELETE /api/vendor/inventory/entries/{id}
     *
     * Note: Only entries with status = FRESH_PO can be deleted
     */
    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Object> deleteInventoryEntry(@PathVariable Long id) {
        logger.info("Received request to delete inventory entry with ID: {}", id);

        try {
            inventoryEntryService.deleteInventoryEntry(id);
            logger.info("Inventory entry deleted successfully with ID: {}", id);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse("Inventory entry deleted successfully"),
                    HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error deleting inventory entry: {}", e.getMessage(), e);
            ErrorDetails errorDetails = new ErrorDetails(
                    AppConstant.ERROR_CODE_INVALID,
                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                    AppConstant.ERROR_TYPE_VALIDATION,
                    e.getMessage());
            return new ResponseEntity<>(
                    ResponseBuilder.getErrorResponse(errorDetails),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Check if a TC number already exists for a vendor
     * GET /api/vendor/inventory/check-tc-uniqueness
     */
    @GetMapping("/check-tc-uniqueness")
    public ResponseEntity<Object> checkTcUniqueness(
            @RequestParam String tcNumber,
            @RequestParam String vendorCode) {
        logger.info("Received request to check TC uniqueness: {} for vendor: {}", tcNumber, vendorCode);

        try {
            boolean exists = inventoryEntryService.existsByTcNumber(tcNumber, vendorCode);
            return new ResponseEntity<>(ResponseBuilder.getSuccessResponse(exists), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error checking TC uniqueness: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(false),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
