package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

@Data
public class MorSampleRequestDto {

    private String samplingDate;

    private String concreteGrade;

    private String plantType;

    private String shedLine;

    private String sampleIdentificationNumber;

    private Long createdBy;

    private Long updatedBy;

}
