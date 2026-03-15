package com.sarthi.Sleeper.dto;


import lombok.Data;

import java.time.LocalDate;

@Data
public class ModulusOfFailureResponseDto {

    private Long id;

    private LocalDate samplingDate;

    private String concreteGrade;

    private String plantType;

    private String shedLineNumber;

    private String batchNo;

    private LocalDate castingDate;

    private String benchGangNumber;

    private String mouldNo;

    private String sampleIdentification;

    private String mrResult;

    private String sampleType;

}