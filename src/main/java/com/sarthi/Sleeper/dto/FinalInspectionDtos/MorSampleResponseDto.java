package com.sarthi.Sleeper.dto.FinalInspectionDtos;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MorSampleResponseDto {

    private Long id;

    private String samplingDate;

    private String shift;

    private String lineNo;

    private String concreteGrade;

    private String plantType;

    private String shedLine;

    private String sampleIdentificationNumber;

    private Long waterCubeStrengthTestId;

    private String batchNumber;

    private String castingDate;

    private Integer mrSamplesRequired;

    private String mrTestType;

    private String status;
    private String overallResult;

    private List<MorSampleDetailDto> details;

    private List<MorTestResultDto> testResults;
    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;

    private LocalDateTime updatedDate;

}