package com.sarthi.SRailPad.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RailTransitionActionReqDto {

    private Long workflowTransitionId;

    private Long moduleId;

    private String requestId;

    private String action;

    private String remarks;

    private Long actionBy;
    private String shift;

    // Cancellation specific fields
    private String materialAvailable;
    private String cancellationBasis;
    private String visitStatus;
    private List<String> cancellationReasons;
    private String cancellationDescription;
    private BigDecimal materialValue;
    private BigDecimal cancellationPercentage;
    private BigDecimal calculatedCharges;
    private BigDecimal maximumCap;
    private BigDecimal finalCancellationCharges;
    private String documentName;
    private String vendorCode;
    private String pincode;
    private String updatedBy;
}
