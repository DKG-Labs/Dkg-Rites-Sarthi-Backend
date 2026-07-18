package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class SleeperRemapSubmitDto {
    private String callNo;
    private String plantId;
    private Integer oldUserId;
    private Integer newUserId;
}
