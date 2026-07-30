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
public class ProcessIcSaveChangesDTO {
    private String icNumber;
    private Long certificateId;
    private String bookNo;
    private String setNo;
    private String offeredInstallmentNo;
    private String passedInstallmentNo;
    private String consignee;
    private String contractRef;
    private String maNumberAndDate;
    private String billPayingOfficer;
    private String purchasingAuthority;
    private String description;
    private String manufacturer;
    private String qapNo;
    private String chpClause;
    private String inspectionDate;
    private String manDays;
    
    // Read-only audit fields
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
