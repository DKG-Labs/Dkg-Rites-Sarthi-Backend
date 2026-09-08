package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class BadSleeperDto {

    private Long sleeperId;
    private String sleeperNo;
    private String reason;
    private Long moduleId;
    private String moduleName;
    private Boolean callRaised = false;
}

