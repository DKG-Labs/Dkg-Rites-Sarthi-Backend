package com.sarthi.SRailPad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RailpadProcessIcEditDTO {
    private String icNumber;
    private String bookNo;
    private String setNo;
    private String installmentNo;
    private String contractor;
    private String contractRef;
    private String billPayingOfficer;
    private String consignee;
    private String purchasingAuthority;
    private String description;
    private String drgNo;
    private String specNo;
    private String qapNo;
    private String typeOfInspection;
    private String chpClNo;
    private String lotNo;
    private String qtyNowOffered;
    private String qtyNowPassed;
    private String qtyNowRejected;
    private String quantityNowPassedText;
    private String reasonsForRejection;
    private String dateOfCall;
    private String noOfVisits;
    private String datesOfInspection;
    private String sealingPattern;
    private String inspectingEngineer;

    // Audit
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
