package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class SleeperTransitionActionReqDto {

    private Long workflowTransitionId;

    private Long moduleId;

    private String requestId;

    private String action;

    private String remarks;

    private Long actionBy;

    private String bookNo;

    private String setNo;


}
