package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;
import java.util.List;

@Data
public class WaterCubeSampleRequestDto {

    private Long productionDeclarationId;

    private String castingDate;

    private String batchNumber;

    private String shift;

    private String lineNo;

    private String concreteGrade;

    private List<WaterCubeSampleDetailDto> details;

    private Long createdBy;

    private Long updatedBy;
}
