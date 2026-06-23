package com.sarthi.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for dimensional check samples per heat.
 * Stores all 20 sample diameters in a single record per heat.
 */
@Data
public class RmDimensionalCheckDto {

    private String inspectionCallNo;
    private String heatNo;
    private Integer heatIndex;
    private String tcNumber;
    private String coilCode;

    // Array of 20 sample diameters
    private List<BigDecimal> sampleDiameters;
    
    // Explicitly stored defect properties
    private Integer defectCount;
    
    // Acceptance status (from RMHeatFinalResult.dimensionalStatus)
    private String status;
}

