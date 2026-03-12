package com.sarthi.dto.processmaterial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for carrying the sum of accepted quantities across all process stages
 * for a specific inspection call and lot number.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStageAcceptedQtyDto {

    private String inspectionCallNo;
    private String lotNumber;

    private Long totalShearingManufactured;
    private Long totalShearingAccepted;
    
    private Long totalTurningManufactured;
    private Long totalTurningAccepted;
    
    private Long totalMpiManufactured;
    private Long totalMpiAccepted;
    
    private Long totalForgingManufactured;
    private Long totalForgingAccepted;
    
    private Long totalQuenchingManufactured;
    private Long totalQuenchingAccepted;
    
    private Long totalTemperingManufactured;
    private Long totalTemperingAccepted;

}
