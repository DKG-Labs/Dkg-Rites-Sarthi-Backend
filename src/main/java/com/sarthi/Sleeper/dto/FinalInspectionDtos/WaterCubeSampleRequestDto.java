package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

import java.util.List;

@Data
public class WaterCubeSampleRequestDto {

    private Long productionDeclarationId;

    private String batchNumber;

    private String castingDate;

    private String shift;

    private String lineNo;

    private String concreteGrade;

    private Long createdBy;

    private String vendorCode;
    private String plantId;

    private List<WaterCubeSampleDetailDto> details;
}
