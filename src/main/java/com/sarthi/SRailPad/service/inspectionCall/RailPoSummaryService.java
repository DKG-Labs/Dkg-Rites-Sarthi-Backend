package com.sarthi.SRailPad.service.inspectionCall;

import com.sarthi.SRailPad.dto.RailPoSummaryDto;

/**
 * Railpad-specific service for fetching PO summary data for the
 * Inspection Initiation screen.
 *
 * Completely isolated from the shared Sleeper PoDataService.
 */
public interface RailPoSummaryService {

    /**
     * Get PO summary data for a given RailInspectionCall.
     *
     * @param poNo   The PO number (e.g. 60260074102063)
     * @param poSrNo The PO item serial number (e.g. "001").
     *               If null, falls back to the first item.
     * @return RailPoSummaryDto or null if PO not found
     */
    RailPoSummaryDto getSummaryByPoAndSr(String poNo, String poSrNo);
}
