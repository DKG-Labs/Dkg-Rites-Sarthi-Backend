package com.sarthi.Sleeper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialConsumptionDto {
    private String id; // String to match frontend format, like "USED-HTS-1234"
    private Long numericId; // For backend DB PK mapping if needed
    private String date; // "YYYY-MM-DD"
    private String rawMaterial;
    private String subType;
    private String usedFor;
    private Integer sleepersMade;
    private Double estimatedQty;
    private Double qty; // actualQty
    private String status;
    private String plantId;
    
    private String vendorCode;
    private Integer createdBy;
    private Integer updatedBy;
    private String workflowStatus;
    private String workflowRemarks;
}
