package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

@Data
public class MorTestResultDto {
    private Long id;
    private String benchNumber;
    private String sleeperNo;
    private Double ctKn;
    private Double cbKn;
    private Double rsKn;
    private Double weight;
    private Double loadKn;
    private Double strength;
    private String result;
    private String remarks;
    private Boolean isPass;
    private String testDate;
    private Long createdBy;
}
