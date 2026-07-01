package com.sarthi.service;

import com.sarthi.dto.VendorInspectionCallStatusDto;
import java.util.List;

/**
 * Service interface for Vendor Inspection Call operations.
 * Provides methods to fetch inspection calls with workflow status for vendors.
 */
public interface VendorInspectionCallService {

    /**
     * Get all inspection calls for a vendor with their latest workflow status.
     * Joins inspection_calls and workflow_transition tables to get current status.
     * 
     * @param vendorId Vendor ID to filter inspection calls
     * @return List of inspection calls with workflow status
     */
    List<VendorInspectionCallStatusDto> getVendorInspectionCallsWithStatus(String vendorId);

    /**
     * Fetch and merge TC Documents for a given call.
     * @param callNo The call number (icNumber)
     * @return byte[] of the merged PDF
     */
    byte[] getTcDocsByCallNo(String callNo);
}
