package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Data;

@Data
public class RejectedDto {

    private Long sleeperId;
    private String sleeperCode;
    private String reason;
    private String type;
}
