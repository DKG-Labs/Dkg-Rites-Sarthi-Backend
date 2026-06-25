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
public class RailpadFinalIcEditDTO {
    private String icNumber; // This will map to callNo
    private String bookNo;
    private String setNo;
    private String offeredInstNo;
    private String passedInstNo;
    private String contractRef;
    private String billPayingOfficer;
    private String consignee;
    private String purchasingAuthority;
    private String description;
    
    private String qtyOfferedPreviously;
    private String qtyPassedPreviously;
    private String qtyNowRejected;
    private String qtyStillDue;
    private String quantityNowPassedText;
    
    private String noOfItemsChecked;
    private String datesOfInspection;
    private String trRecDate;
    
    private String sealingPattern;
    private String facsimileText;
    private String reasonsForRejection;
    private String inspectingEngineer;

    // Read-only audit fields
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
