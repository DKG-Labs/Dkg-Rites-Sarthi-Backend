package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WaterCubeSampleResponseDto {

    private Long id;

    private Long productionDeclarationId;

    private String batchNumber;

    private String castingDate;

    private String shift;

    private String lineNo;

    private String concreteGrade;

    private Long createdBy;

    private LocalDateTime createdDate;

    private Long updatedBy;

    private LocalDateTime updatedDate;

    private String vendorCode;
    private String plantId;

    private List<WaterCubeSampleDetailDto> details;
}
