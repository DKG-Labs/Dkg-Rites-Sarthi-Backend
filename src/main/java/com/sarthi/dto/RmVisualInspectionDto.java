package com.sarthi.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for visual inspection defects per heat.
 * Stores all defect selections and lengths in a single record per heat.
 */
@Data
public class RmVisualInspectionDto {

    private String inspectionCallNo;
    private String heatNo;
    private Integer heatIndex;

    // Defect selections as a map: defectName -> isSelected
    private Map<String, Boolean> defects;

    // Defect lengths as a map: defectName -> length
    private Map<String, BigDecimal> defectLengths;

    // Explicitly stored defect properties
    private String otherRemarks;
    private String allDefectsRemark;
    private Integer defectCount;
    private BigDecimal weightRejected;
}

