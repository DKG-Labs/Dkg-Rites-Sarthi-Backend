package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

import java.util.List;

@Data
public class SleeperInspectionDto {

    private Long sleeperId;
    private String sleeperNo;
    private String result;
    private String rejectionReason;

    private List<ParameterInspectionDto> parameters;

}