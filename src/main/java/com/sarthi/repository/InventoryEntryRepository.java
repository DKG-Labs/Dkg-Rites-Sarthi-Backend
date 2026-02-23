package com.sarthi.repository;

import com.sarthi.entity.InventoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InventoryEntry entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface InventoryEntryRepository extends JpaRepository<InventoryEntry, Long> {

    /**
     * Find all inventory entries by vendor code
     * 
     * @param vendorCode The vendor code to search for
     * @return List of inventory entries for the vendor
     */
    List<InventoryEntry> findByVendorCodeOrderByCreatedDateDesc(String vendorCode);

    /**
     * Find inventory entries by vendor code and status
     * 
     * @param vendorCode The vendor code
     * @param status     The inventory status
     * @return List of inventory entries matching the criteria
     */
    List<InventoryEntry> findByVendorCodeAndStatus(String vendorCode, InventoryEntry.InventoryStatus status);

    /**
     * Find inventory entries by heat number
     * 
     * @param heatNumber The heat/batch/lot number
     * @return List of inventory entries with the heat number
     */
    List<InventoryEntry> findByHeatNumber(String heatNumber);

    /**
     * Find inventory entries by sub PO number
     * 
     * @param subPoNumber The sub PO number
     * @return List of inventory entries with the sub PO number
     */
    List<InventoryEntry> findBySubPoNumber(String subPoNumber);

    /**
     * Check if an entry exists with the given heat number and vendor code
     * 
     * @param heatNumber The heat number
     * @param vendorCode The vendor code
     * @return true if exists, false otherwise
     */
    boolean existsByHeatNumberAndVendorCode(String heatNumber, String vendorCode);

    /**
     * Find inventory entry by heat number and TC number combination
     * 
     * @param heatNumber The heat number
     * @param tcNumber   The TC number
     * @return Optional inventory entry matching both criteria
     */
    Optional<InventoryEntry> findByHeatNumberAndTcNumber(String heatNumber, String tcNumber);

    /**
     * Check if an entry exists with the given TC number and vendor code
     * 
     * @param tcNumber   The TC number
     * @param vendorCode The vendor code
     * @return true if exists, false otherwise
     */
    boolean existsByTcNumberAndVendorCode(String tcNumber, String vendorCode);

    /**
     * Find all inventory entries for a vendor with FRESH_PO status (available
     * inventory)
     * 
     * @param vendorCode The vendor code
     * @param status     The inventory status (FRESH_PO for available)
     * @return List of available inventory entries
     */
    List<InventoryEntry> findByVendorCodeAndStatusOrderByCreatedDateDesc(String vendorCode,
            InventoryEntry.InventoryStatus status);

    /**
     * Find all inventory entries for a set of heat numbers (batch fetch to avoid N+1)
     *
     * @param heatNumbers list of heat numbers
     * @return List of inventory entries matching any of the heat numbers
     */
    List<InventoryEntry> findByHeatNumberIn(List<String> heatNumbers);
}
