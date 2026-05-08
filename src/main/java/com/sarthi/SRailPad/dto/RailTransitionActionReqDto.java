package com.sarthi.SRailPad.dto;

import lombok.Data;

@Data
public class RailTransitionActionReqDto {

    private Long workflowTransitionId;

    private Long moduleId;

    private String requestId;

    private String action;

    private String remarks;

    private Long actionBy;
    private String shift;
}
