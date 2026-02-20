package com.sarthi.service.Impl;

import com.sarthi.constant.AppConstant;
import com.sarthi.dto.InventoryEntryRequestDto;
import com.sarthi.dto.InventoryBulkEntryRequestDto;
import com.sarthi.dto.InventoryEntryResponseDto;
import com.sarthi.entity.InventoryEntry;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.InventoryEntryRepository;
import com.sarthi.service.InventoryEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for inventory entry operations
 */
@Service
@Transactional
public class InventoryEntryServiceImpl implements InventoryEntryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryEntryServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private InventoryEntryRepository inventoryEntryRepository;

    @Override
    public InventoryEntryResponseDto createInventoryEntry(InventoryEntryRequestDto requestDto) {
        logger.info("Creating inventory entry for vendor: {}", requestDto.getVendorCode());

        try {
            // Validate required fields
            validateInventoryRequest(requestDto);

            // Create entity from DTO
            InventoryEntry entry = new InventoryEntry();
            mapRequestToEntity(requestDto, entry);

            // Set default status
            entry.setStatus(InventoryEntry.InventoryStatus.FRESH_PO);

            // Save to database
            InventoryEntry savedEntry = inventoryEntryRepository.save(entry);
            logger.info("Inventory entry created successfully with ID: {}", savedEntry.getId());

            // Convert to response DTO
            return mapEntityToResponse(savedEntry);

        } catch (Exception e) {
            logger.error("Error creating inventory entry: {}", e.getMessage(), e);
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_INTERNAL,
                            AppConstant.ERROR_TYPE_ERROR,
                            "Failed to create inventory entry: " + e.getMessage()));
        }
    }

    @Override
    public List<InventoryEntryResponseDto> createMultipleInventoryEntries(InventoryBulkEntryRequestDto bulkRequestDto,
            String tcFilePath) {
        logger.info("Creating multiple inventory entries (bulk) for vendor: {}", bulkRequestDto.getVendorCode());

        String finalTcFilePath = tcFilePath;
        if (bulkRequestDto.getTcFileBase64() != null && !bulkRequestDto.getTcFileBase64().isEmpty()) {
            try {
                // Determine a base path for TC files
                String basePath = "uploads/tc_files";
                String fileName = com.sarthi.util.CommonUtils.saveBase64Image(bulkRequestDto.getTcFileBase64(),
                        basePath);
                finalTcFilePath = basePath + "/" + fileName;
                logger.info("TC file saved at: {}", finalTcFilePath);
            } catch (IOException e) {
                logger.error("Error saving TC file: {}", e.getMessage());
            }
        }

        final String effectivelyFinalTcFilePath = finalTcFilePath;

        List<InventoryEntry> entriesToSave = bulkRequestDto.getHeatEntries().stream().map(heatDto -> {
            InventoryEntry entry = new InventoryEntry();

            // Common fields
            entry.setVendorCode(bulkRequestDto.getVendorCode());
            entry.setVendorName(bulkRequestDto.getVendorName());
            entry.setCompanyId(bulkRequestDto.getCompanyId());
            entry.setCompanyName(bulkRequestDto.getCompanyName());
            entry.setSupplierName(bulkRequestDto.getSupplierName());
            entry.setUnitName(bulkRequestDto.getUnitName());
            entry.setSupplierAddress(bulkRequestDto.getSupplierAddress());
            entry.setRawMaterial(bulkRequestDto.getRawMaterial());
            entry.setGradeSpecification(bulkRequestDto.getGradeSpecification());
            entry.setLengthOfBars(bulkRequestDto.getLengthOfBars());
            entry.setTcNumber(bulkRequestDto.getTcNumber());
            entry.setTcDate(parseDate(bulkRequestDto.getTcDate()));
            entry.setUnitOfMeasurement(bulkRequestDto.getUnitOfMeasurement());
            entry.setTcFilePath(effectivelyFinalTcFilePath);

            // Heat-specific fields
            entry.setHeatNumber(heatDto.getHeatNumber());
            entry.setTcQuantity(heatDto.getTcQuantity());
            entry.setQtyLeftForInspection(heatDto.getTcQuantity()); // Initially equal to quantity
            entry.setOfferedQuantity(BigDecimal.ZERO);
            entry.setNumberOfBundles(heatDto.getNumberOfBundles());
            entry.setSubPoNumber(heatDto.getSubPoNumber());
            entry.setSubPoDate(parseDate(heatDto.getSubPoDate()));
            entry.setSubPoQty(heatDto.getSubPoQty());
            entry.setInvoiceNumber(heatDto.getInvoiceNumber());
            entry.setInvoiceDate(parseDate(heatDto.getInvoiceDate()));

            entry.setStatus(InventoryEntry.InventoryStatus.FRESH_PO);

            return entry;
        }).collect(Collectors.toList());

        try {
            List<InventoryEntry> savedEntries = inventoryEntryRepository.saveAll(entriesToSave);
            logger.info("Saved {} inventory entries for TC: {}", savedEntries.size(), bulkRequestDto.getTcNumber());

            return savedEntries.stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Database error while saving bulk entries: {}", e.getMessage(), e);
            throw new BusinessException(new ErrorDetails(
                    AppConstant.ERROR_CODE_RESOURCE,
                    AppConstant.ERROR_TYPE_CODE_INTERNAL,
                    AppConstant.ERROR_TYPE_ERROR,
                    "Database error: " + e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryEntryResponseDto> getInventoryEntriesByVendor(String vendorCode) {
        logger.info("Fetching inventory entries for vendor: {}", vendorCode);

        try {
            List<InventoryEntry> entries = inventoryEntryRepository.findByVendorCodeOrderByCreatedDateDesc(vendorCode);
            logger.info("Found {} inventory entries for vendor: {}", entries.size(), vendorCode);

            return entries.stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error fetching inventory entries: {}", e.getMessage(), e);
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_INTERNAL,
                            AppConstant.ERROR_TYPE_ERROR,
                            "Failed to fetch inventory entries: " + e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryEntryResponseDto getInventoryEntryById(Long id) {
        logger.info("Fetching inventory entry by ID: {}", id);

        InventoryEntry entry = inventoryEntryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Inventory entry not found with ID: " + id)));

        return mapEntityToResponse(entry);
    }

    @Override
    public InventoryEntryResponseDto updateInventoryStatus(Long id, String status) {
        logger.info("Updating inventory entry {} status to: {}", id, status);

        InventoryEntry entry = inventoryEntryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Inventory entry not found with ID: " + id)));

        try {
            InventoryEntry.InventoryStatus newStatus = InventoryEntry.InventoryStatus.valueOf(status.toUpperCase());
            entry.setStatus(newStatus);
            InventoryEntry updatedEntry = inventoryEntryRepository.save(entry);

            logger.info("Inventory entry status updated successfully");
            return mapEntityToResponse(updatedEntry);

        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Invalid status value: " + status));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryEntryResponseDto getInventoryEntryByHeatAndTc(String heatNumber, String tcNumber) {
        logger.info("Fetching inventory entry by heat number: {} and TC number: {}", heatNumber, tcNumber);

        return inventoryEntryRepository.findByHeatNumberAndTcNumber(heatNumber, tcNumber)
                .map(this::mapEntityToResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public InventoryEntryResponseDto updateInventoryEntry(Long id, InventoryEntryRequestDto requestDto) {
        logger.info("Updating inventory entry with ID: {}", id);

        // Validate request
        validateInventoryRequest(requestDto);

        // Find existing entry
        InventoryEntry existingEntry = inventoryEntryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Inventory entry not found with ID: " + id)));

        // Only allow updates for FRESH_PO status
        if (existingEntry.getStatus() != InventoryEntry.InventoryStatus.FRESH_PO) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Cannot update inventory entry with status: " + existingEntry.getStatus() +
                                    ". Only FRESH_PO entries can be modified."));
        }

        // Update entity with new data
        mapRequestToEntity(requestDto, existingEntry);

        // Save updated entry
        InventoryEntry updatedEntry = inventoryEntryRepository.save(existingEntry);
        logger.info("Inventory entry updated successfully with ID: {}", updatedEntry.getId());

        return mapEntityToResponse(updatedEntry);
    }

    @Override
    @Transactional
    public void deleteInventoryEntry(Long id) {
        logger.info("Deleting inventory entry with ID: {}", id);

        // Find existing entry
        InventoryEntry existingEntry = inventoryEntryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Inventory entry not found with ID: " + id)));

        // Only allow deletion for FRESH_PO status
        if (existingEntry.getStatus() != InventoryEntry.InventoryStatus.FRESH_PO) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Cannot delete inventory entry with status: " + existingEntry.getStatus() +
                                    ". Only FRESH_PO entries can be deleted."));
        }

        // Delete the entry
        inventoryEntryRepository.delete(existingEntry);
        logger.info("Inventory entry deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public InventoryEntryResponseDto updateOfferedQuantity(String heatNumber, String tcNumber, BigDecimal offeredQty) {
        logger.info("Updating offered quantity for heat: {}, TC: {}, offered: {}", heatNumber, tcNumber, offeredQty);

        // Find inventory entry by heat number and TC number
        InventoryEntry entry = inventoryEntryRepository.findByHeatNumberAndTcNumber(heatNumber, tcNumber)
                .orElseThrow(() -> new BusinessException(
                        new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_CODE_RESOURCE,
                                AppConstant.ERROR_TYPE_RESOURCE,
                                "Inventory entry not found for heat: " + heatNumber + ", TC: " + tcNumber)));

        // Validate that offered quantity doesn't exceed TC Qty Remaining
        BigDecimal tcQtyRemaining = entry.getQtyLeftForInspection() != null ? entry.getQtyLeftForInspection()
                : BigDecimal.ZERO;
        if (offeredQty.compareTo(tcQtyRemaining) > 0) {
            logger.warn("Offered quantity {} exceeds TC Qty Remaining {} for heat: {}, TC: {}",
                    offeredQty, tcQtyRemaining, heatNumber, tcNumber);
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_CODE_RESOURCE,
                            AppConstant.ERROR_TYPE_RESOURCE,
                            "Offered Qty (" + offeredQty + ") cannot be more than TC Qty Remaining with Vendor ("
                                    + tcQtyRemaining + ")"));
        }

        // Update offered quantity (add to existing)
        BigDecimal currentOffered = entry.getOfferedQuantity() != null ? entry.getOfferedQuantity() : BigDecimal.ZERO;
        BigDecimal newOfferedQty = currentOffered.add(offeredQty);
        entry.setOfferedQuantity(newOfferedQty);

        // Recalculate qty_left_for_inspection
        BigDecimal tcQty = entry.getTcQuantity() != null ? entry.getTcQuantity() : BigDecimal.ZERO;
        BigDecimal qtyLeft = tcQty.subtract(newOfferedQty);
        entry.setQtyLeftForInspection(qtyLeft);

        // Update status based on remaining quantity
        if (qtyLeft.compareTo(BigDecimal.ZERO) <= 0) {
            entry.setStatus(InventoryEntry.InventoryStatus.EXHAUSTED);
            logger.info("Inventory entry marked as EXHAUSTED (no quantity left)");
        } else if (entry.getStatus() == InventoryEntry.InventoryStatus.FRESH_PO) {
            entry.setStatus(InventoryEntry.InventoryStatus.UNDER_INSPECTION);
            logger.info("Inventory entry status changed to UNDER_INSPECTION");
        }

        // Save updated entry
        InventoryEntry updatedEntry = inventoryEntryRepository.save(entry);
        logger.info("Inventory quantities updated: offered={}, left={}, status={}",
                newOfferedQty, qtyLeft, updatedEntry.getStatus());

        return mapEntityToResponse(updatedEntry);
    }

    /**
     * Validate inventory request DTO
     */
    private void validateInventoryRequest(InventoryEntryRequestDto dto) {
        if (dto.getVendorCode() == null || dto.getVendorCode().trim().isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Vendor code is required"));
        }
        if (dto.getSupplierName() == null || dto.getSupplierName().trim().isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Supplier name is required"));
        }
        if (dto.getRawMaterial() == null || dto.getRawMaterial().trim().isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Raw material is required"));
        }
        if (dto.getHeatNumber() == null || dto.getHeatNumber().trim().isEmpty()) {
            throw new BusinessException(
                    new ErrorDetails(AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Heat number is required"));
        }
    }

    /**
     * Map request DTO to entity
     */
    private void mapRequestToEntity(InventoryEntryRequestDto dto, InventoryEntry entity) {
        entity.setVendorCode(dto.getVendorCode());
        entity.setVendorName(dto.getVendorName());
        entity.setCompanyId(dto.getCompanyId());
        entity.setCompanyName(dto.getCompanyName());
        entity.setSupplierName(dto.getSupplierName());
        entity.setUnitName(dto.getUnitName());
        entity.setSupplierAddress(dto.getSupplierAddress());
        entity.setRawMaterial(dto.getRawMaterial());
        entity.setGradeSpecification(dto.getGradeSpecification());
        entity.setLengthOfBars(dto.getLengthOfBars());
        entity.setHeatNumber(dto.getHeatNumber());
        entity.setTcNumber(dto.getTcNumber());
        entity.setTcDate(parseDate(dto.getTcDate()));
        entity.setTcQuantity(dto.getTcQuantity());
        
        // If it's a fresh entry, keep qtyLeftForInspection in sync with tcQuantity
        if (entity.getStatus() == InventoryEntry.InventoryStatus.FRESH_PO) {
            entity.setQtyLeftForInspection(dto.getTcQuantity());
        }
        
        entity.setSubPoNumber(dto.getSubPoNumber());
        entity.setSubPoDate(parseDate(dto.getSubPoDate()));
        entity.setSubPoQty(dto.getSubPoQty());
        entity.setInvoiceNumber(dto.getInvoiceNumber());
        entity.setInvoiceDate(parseDate(dto.getInvoiceDate()));
        entity.setUnitOfMeasurement(dto.getUnitOfMeasurement());
        entity.setRateOfMaterial(dto.getRateOfMaterial());
        entity.setRateOfGst(dto.getRateOfGst());
        entity.setBaseValuePo(dto.getBaseValuePo());
        entity.setTotalPo(dto.getTotalPo());
        entity.setNumberOfBundles(dto.getNumberOfBundles());
    }

    /**
     * Map entity to response DTO
     */
    private InventoryEntryResponseDto mapEntityToResponse(InventoryEntry entity) {
        InventoryEntryResponseDto dto = new InventoryEntryResponseDto();
        dto.setId(entity.getId());
        dto.setVendorCode(entity.getVendorCode());
        dto.setVendorName(entity.getVendorName());
        dto.setCompanyId(entity.getCompanyId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setSupplierName(entity.getSupplierName());
        dto.setUnitName(entity.getUnitName());
        dto.setSupplierAddress(entity.getSupplierAddress());
        dto.setRawMaterial(entity.getRawMaterial());
        dto.setGradeSpecification(entity.getGradeSpecification());
        dto.setLengthOfBars(entity.getLengthOfBars());
        dto.setHeatNumber(entity.getHeatNumber());
        dto.setTcNumber(entity.getTcNumber());
        dto.setTcDate(entity.getTcDate());
        dto.setTcQuantity(entity.getTcQuantity());
        dto.setOfferedQuantity(entity.getOfferedQuantity());
        dto.setQtyLeftForInspection(entity.getQtyLeftForInspection());
        dto.setSubPoNumber(entity.getSubPoNumber());
        dto.setSubPoDate(entity.getSubPoDate());
        dto.setSubPoQty(entity.getSubPoQty());
        dto.setInvoiceNumber(entity.getInvoiceNumber());
        dto.setInvoiceDate(entity.getInvoiceDate());
        dto.setUnitOfMeasurement(entity.getUnitOfMeasurement());
        dto.setRateOfMaterial(entity.getRateOfMaterial());
        dto.setRateOfGst(entity.getRateOfGst());
        dto.setBaseValuePo(entity.getBaseValuePo());
        dto.setTotalPo(entity.getTotalPo());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        dto.setCreatedAt(entity.getCreatedDate());
        dto.setUpdatedAt(entity.getUpdatedDate());
        dto.setNumberOfBundles(entity.getNumberOfBundles());
        dto.setTcFilePath(entity.getTcFilePath());
        return dto;
    }

    /**
     * Parse date string to LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            logger.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTcNumber(String tcNumber, String vendorCode) {
        logger.info("Checking if TC number {} exists for vendor: {}", tcNumber, vendorCode);
        return inventoryEntryRepository.existsByTcNumberAndVendorCode(tcNumber, vendorCode);
    }
}
