package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalIcSaveChangesDTO {
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
    private String trRecDate;
    
    // Read-only audit fields
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
