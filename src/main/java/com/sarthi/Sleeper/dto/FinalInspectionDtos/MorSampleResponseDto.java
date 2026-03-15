package com.sarthi.Sleeper.dto.FinalInspectionDtos;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MorSampleResponseDto {

    private Long id;

    private String samplingDate;

    private String concreteGrade;

    private String plantType;

    private String shedLine;

    private String sampleIdentificationNumber;
    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;

    private LocalDateTime updatedDate;

}