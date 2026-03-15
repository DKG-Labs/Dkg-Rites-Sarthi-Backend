package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;
import java.util.List;

@Data
public class MorSampleRequestDto {

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

    private List<MorSampleDetailDto> details;

    private Long createdBy;

    private Long updatedBy;

}
