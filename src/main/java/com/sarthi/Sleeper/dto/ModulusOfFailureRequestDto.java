package com.sarthi.Sleeper.dto;


import lombok.Data;

@Data
public class ModulusOfFailureRequestDto {

    private String samplingDate;

    private String concreteGrade;

    private String plantType;

    private String shedLineNumber;

    private String batchNo;

    private String castingDate;

    private String benchGangNumber;

    private String mouldNo;

    private String sampleIdentification;

    private String mrResult;

    private String sampleType;

    private Long createdBy;

    private Long updatedBy;
}
