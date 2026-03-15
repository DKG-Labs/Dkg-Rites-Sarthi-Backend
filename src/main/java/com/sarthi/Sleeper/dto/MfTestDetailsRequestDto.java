package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class MfTestDetailsRequestDto {

    private Long modulusOfFailureId;

    private String testingDate;

    private Double strength;

    private String remarks;

    private Long createdBy;

    private Long updatedBy;
}