package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

@Data
public class SleeperDto {

    private Long sleeperId;
    private String sleeperNo;
    private String status;

    public Long moduleId;

    private Boolean callRaised = false;

    /** Set when the sleeper is rejected with its whole lot (e.g. water cube test failed). */
    private String rejectionReason;
}
