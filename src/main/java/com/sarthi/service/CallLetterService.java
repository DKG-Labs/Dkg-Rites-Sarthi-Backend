package com.sarthi.service;

import com.sarthi.dto.CallLetterDetailsDto;

/**
 * Service interface for generating call letter details
 * by aggregating data from multiple tables.
 */
public interface CallLetterService {

    /**
     * Fetch enriched call letter details for a given requestId (IC Number).
     *
     * @param requestId the inspection call number e.g. "ER-03280001"
     * @return CallLetterDetailsDto with all fields required by the PDF generator
     */
    CallLetterDetailsDto getCallLetterDetails(String requestId);
}
