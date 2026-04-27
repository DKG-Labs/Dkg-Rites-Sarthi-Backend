package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

@Data
public class SleeperDto {

    private Long sleeperId;
    private String sleeperNo;
    private String status;

    public Long moduleId;

    private Boolean callRaised = false;
}
