package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;
import java.util.List;

@Data
public class WaterCubeStrengthTestRequestDto {

    private Long productionDeclarationId;
    private Long waterCubeSampleDeclarationId;
    
    private String batchNumber;
    private String concreteGrade;
    private String castingDate;
    private String shift;
    private String lineNo;
    
    private Double fckTarget;
    private Integer ageDays;
    
    private Double s1Avg;
    private Double s2Avg;
    private Double avgX;
    private Double minY;
    
    private Double s1Variation;
    private Double s2Variation;
    
    private Boolean condition1;
    private Boolean condition2;
    private Boolean condition3;
    
    private Integer mrSamplesRequired;
    private String finalTestResult;
    
    private Long createdBy;

    private List<WaterCubeStrengthDetailRequestDto> details;
}
