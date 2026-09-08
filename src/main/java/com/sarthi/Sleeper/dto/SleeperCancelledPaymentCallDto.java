package com.sarthi.Sleeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleeperCancelledPaymentCallDto {
    private Integer workflowTransitionId;
    private String callNo;
    private String status;
    private String cancelRemarks;
    private String action;
    private String poNo;
    private String srNo;
    private String ibsCaseNo;
    private String ibsCallNo;
    private Long offeredQty;
    private String callDate;
    private String sleeperType;
    private Double basePayableAmount;
    private Double gst;
    private Double totalPayableAmount;
    private String bankAccountDetails;
    private String paymentReason;
    private String chargeType;
    private String paymentStatus;
    private String plantId;
    private String vendorCode;
    private String rio;
    private String rioEmail;
    private String documentName;
    private LocalDateTime createdDate;
}
