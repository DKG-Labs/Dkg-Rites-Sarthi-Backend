package com.sarthi.udmcomplaint.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/** What the CM forms need: current status, whether the user may act, flags and dropdown values. */
@Data
public class CmContextDto {
    private String complaintStatus;
    private boolean canAct;
    /** Next form for the current status: DECISION | JI_COMPLETE | AUDIT_COMPLETE | RCA | CAPA | null. */
    private String nextStage;
    /** Failed within 90 days of IC / material receipt; null when dates are missing. */
    private Boolean failedWithin90Days;
    private Long daysToFailure;
    private Map<String, List<String>> dropdowns;
}
