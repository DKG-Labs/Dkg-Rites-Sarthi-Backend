package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleeperFinalIcSaveChangesDTO {
    private String icNumber;
    private Long certificateId;
    private String bookNo;
    private String setNo;
    private String offeredInstallmentNo;
    private String passedInstallmentNo;
    private String consignee;
    private String cummQtyOfferedPrev;
    private String qtyPrevPassed;
    private String qtyStillDue;
    private String maNumberAndDate;
    private String purchasingAuthority;
    private String description;
    private String manufacturer;
    private String trRecDate;
    private String noOfVisits;
    private String datesOfInspection;
    private String sealingPattern;
    private String reasonsForRejection;
    private String facsimileText;
    private String inspectingEngineer;
    private String status;

    // Audit fields
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
