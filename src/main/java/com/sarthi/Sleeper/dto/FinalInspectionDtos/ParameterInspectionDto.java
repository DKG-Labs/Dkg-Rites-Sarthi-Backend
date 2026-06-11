package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

@Data
public class ParameterInspectionDto {

    private Long parameterId;
    private String result;
    private Long mainReasonId;

    private Long subReasonId;

}