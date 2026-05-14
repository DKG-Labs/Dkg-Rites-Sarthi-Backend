package com.sarthi.service;

import com.sarthi.dto.po.PoDataForSectionsDto;

/**
 * Service interface for fetching PO data for Inspection Initiation Sections
 */
public interface PoDataService {

    /**
     * Get PO data for Sections A, B, C by PO Number
     * 
     * @param poNo PO Number
     * @return PoDataForSectionsDto containing all required data
     */
    PoDataForSectionsDto getPoDataByPoNumber(String poNo);

    /**
     * Get PO data for Section C with RM inspection details by PO Number
     * 
     * @param poNo      PO Number
     * @param requestId Optional Inspection Call Number to filter specific
     *                  inspection call
     * @return PoDataForSectionsDto containing Section C data with RM heat details
     */
    PoDataForSectionsDto getPoDataWithRmDetailsForSectionC(String poNo, String requestId);

    /**
     * Update color code for a specific heat number
     * 
     * @param heatId    Heat ID from rm_heat_quantities table
     * @param colorCode Color code to update
     * @return true if updated successfully, false if heat not found
     */
    boolean updateColorCode(Integer heatId, String colorCode);
}
