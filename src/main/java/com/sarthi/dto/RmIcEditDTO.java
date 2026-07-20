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
public class RmIcEditDTO {
    private String icNumber;
    private Long certificateId;
    private String bookNo;
    private String setNo;
    private String offeredInstallmentNo;
    private String passedInstallmentNo;
    private String drawingNo;
    private String manufacturer;
    private String contractorPo;
    private String consigneeRailway;
    private String consigneeManufacturer;
    private String purchasingAuthority;
    private String description;
    private String specNo;
    private String qapNo;
    private String chpClause;
    private String visitsNo;
    private String inspectionDate;
    
    // Read-only audit fields
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
