package com.sarthi.udmcomplaint.service;

import com.sarthi.udmcomplaint.dto.SbuContextDto;
import com.sarthi.udmcomplaint.dto.SbuReviewRequest;
import com.sarthi.udmcomplaint.dto.UdmComplaintStatusDto;

/** Phase 3a — SBU Head review (SRS §5 stage 6). */
public interface UdmComplaintSbuService {

    SbuContextDto getSbuContext(Long udmRejectionId, Integer userId);

    /** FORWARD → Railway Board; RETURN → CM rework (remarks mandatory, next round). */
    UdmComplaintStatusDto review(Long udmRejectionId, SbuReviewRequest request, Integer userId);
}
