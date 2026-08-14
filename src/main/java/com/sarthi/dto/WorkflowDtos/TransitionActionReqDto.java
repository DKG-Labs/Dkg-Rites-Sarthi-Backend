package com.sarthi.dto.WorkflowDtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransitionActionReqDto {

    private Integer workflowTransitionId;
    private String requestId;

    private String action;                  // VERIFY / RETURN_TO_VENDOR / FIX_ROUTING

    private String remarks;

    private Integer actionBy;               // userId

    private String pincode;

    private String rioRouteChange;


    private String poStatus;         // OK / NOT_OK
    private String cmDecision;       // APPROVED / REJECTED
    private String paymentType;      // PAYABLE / NON_PAYABLE  --backend logic fetch no need form ui
    private String paymentCompleted;  //true  --backend logic fetch no need form ui
    private String materialAvailable;// YES / NO
    private String resultStatus;     // OK / PARTIAL / NOT_OK
    private String cmFinalApproval;  // APPROVED

    private String materialName;

   private String paymentReceivedStatus;  // after verifed by Rio user then if raw material then we need pas the Yess or No


   private Integer assignUserId; // only when cm re route inspection call  rio user

    private String updatedBy; // User ID who is updating the inspection
    private String shiftCode;

    private String sbuHeadDecision;

    private int inspectedQty;

    private String lotNo;

    private int offeredQty;

    private int manufacturedQty;

    private String heatNo;
    private BigDecimal rejectedQty;

    private java.time.LocalDate dateOfInspection;

    // SRS Call Cancellation Fields
    private String cancellationBasis;           // CHARGEABLE / NON_CHARGEABLE
    private String visitStatus;                 // BEFORE_VISIT / AFTER_VISIT
    private java.util.List<String> cancellationReasons;
    private String cancellationDescription;
    private BigDecimal materialValue;
    private BigDecimal cancellationPercentage;
    private BigDecimal calculatedCharges;
    private BigDecimal maximumCap;
    private BigDecimal finalCancellationCharges;
    private String documentName;
    private String vendorCode;
}


