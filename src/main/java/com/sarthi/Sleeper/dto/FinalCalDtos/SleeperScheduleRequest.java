package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SleeperScheduleRequest {

    private String callNo;
    private Long workflowTransitionId;

    private LocalDate scheduleDate;
    private String reason;

    private String plantId;
    private String vendorCode;
    private String shift;

    private Long createdBy;

    private Long updatedBy;
}
