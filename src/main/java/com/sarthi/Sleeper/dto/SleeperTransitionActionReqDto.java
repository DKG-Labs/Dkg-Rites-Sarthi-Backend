package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class SleeperTransitionActionReqDto {

    private Long workflowTransitionId;

    private Long moduleId;

    private String requestId;

    private String action;

    private String remarks;

    private Long actionBy;

    private String bookNo;

    private String setNo;

    // Cancellation specific fields
    private String cancellationBasis;
    private String visitStatus;
    private String reasons;
    private java.util.List<String> cancellationReasons;
    private String cancellationDescription;
    private java.math.BigDecimal materialValue;
    private java.math.BigDecimal percentage;
    private java.math.BigDecimal cancellationPercentage;
    private java.math.BigDecimal calculatedCharges;
    private java.math.BigDecimal maximumCap;
    private java.math.BigDecimal finalCancellationCharges;
    private String documentName;
    private String vendorCode;
    private String pincode;
    private String materialAvailable;
    private String updatedBy;
}
