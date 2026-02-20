package com.sarthi.service;

import com.sarthi.dto.InventoryEntryRequestDto;
import com.sarthi.dto.InventoryBulkEntryRequestDto;
import com.sarthi.dto.InventoryEntryResponseDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for inventory entry operations
 */
public interface InventoryEntryService {

    /**
     * Create a new inventory entry
     * 
     * @param requestDto The inventory entry data
     * @return The created inventory entry
     */
    InventoryEntryResponseDto createInventoryEntry(InventoryEntryRequestDto requestDto);

    /**
     * Create multiple inventory entries with multiple heats against one TC
     *
     * @param bulkRequestDto The bulk inventory entry data
     * @param tcFilePath     Optional path to the uploaded TC PDF file
     * @return List of created inventory entries
     */
    List<InventoryEntryResponseDto> createMultipleInventoryEntries(InventoryBulkEntryRequestDto bulkRequestDto,
            String tcFilePath);

    /**
     * Get all inventory entries for a vendor
     * 
     * @param vendorCode The vendor code
     * @return List of inventory entries
     */
    List<InventoryEntryResponseDto> getInventoryEntriesByVendor(String vendorCode);

    /**
     * Get inventory entry by ID
     * 
     * @param id The inventory entry ID
     * @return The inventory entry
     */
    InventoryEntryResponseDto getInventoryEntryById(Long id);

    /**
     * Update inventory entry status
     * 
     * @param id     The inventory entry ID
     * @param status The new status
     * @return The updated inventory entry
     */
    InventoryEntryResponseDto updateInventoryStatus(Long id, String status);

    /**
     * Get inventory entry by heat number and TC number combination
     * 
     * @param heatNumber The heat number
     * @param tcNumber   The TC number
     * @return The inventory entry matching both criteria, or null if not found
     */
    InventoryEntryResponseDto getInventoryEntryByHeatAndTc(String heatNumber, String tcNumber);

    /**
     * Update an existing inventory entry
     * 
     * @param id         The inventory entry ID
     * @param requestDto The updated inventory entry data
     * @return The updated inventory entry
     */
    InventoryEntryResponseDto updateInventoryEntry(Long id, InventoryEntryRequestDto requestDto);

    /**
     * Delete an inventory entry
     * Only allowed for entries with status = FRESH_PO
     * 
     * @param id The inventory entry ID
     */
    void deleteInventoryEntry(Long id);

    /**
     * Update inventory offered quantity when an inspection call is created
     * 
     * @param heatNumber The heat number
     * @param tcNumber   The TC number
     * @param offeredQty The quantity offered in the inspection call
     * @return The updated inventory entry
     */
    InventoryEntryResponseDto updateOfferedQuantity(String heatNumber, String tcNumber, BigDecimal offeredQty);

    /**
     * Check if a TC number already exists in inventory for a vendor
     * 
     * @param tcNumber   The TC number to check
     * @param vendorCode The vendor code
     * @return true if exists, false otherwise
     */
    boolean existsByTcNumber(String tcNumber, String vendorCode);
}
