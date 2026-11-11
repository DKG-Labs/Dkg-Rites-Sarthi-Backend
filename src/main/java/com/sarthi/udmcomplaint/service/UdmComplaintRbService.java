package com.sarthi.udmcomplaint.service;

import com.sarthi.udmcomplaint.dto.RbContextDto;
import com.sarthi.udmcomplaint.dto.RbReviewRequest;
import com.sarthi.udmcomplaint.dto.UdmComplaintStatusDto;

/** Phase 3b — Railway Board review (SRS §5 stage 7). */
public interface UdmComplaintRbService {

    RbContextDto getRbContext(Long udmRejectionId, Integer userId);

    /** APPROVE → CLOSED; RETURN → back to SBU Head (remarks mandatory). */
    UdmComplaintStatusDto review(Long udmRejectionId, RbReviewRequest request, Integer userId);
}
