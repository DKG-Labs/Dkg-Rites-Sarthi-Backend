package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MfTestDetailsRequestDto {

    private Long modulusOfFailureId;

    private String testingDate;

    private Double strength;


    private String remarks;

    private Long createdBy;

    private Long updatedBy;

    private String shift;
    private String vendorCode;
    private String plantId;

    private String batchNo;

    private LocalDate castingDate;
    private String sampleIdentification;

    private String concreteGrade;
    private String result;
}