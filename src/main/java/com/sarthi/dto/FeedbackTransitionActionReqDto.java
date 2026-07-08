package com.sarthi.dto;

import lombok.Data;

@Data
public class FeedbackTransitionActionReqDto {

    private Long workflowTransitionId;

    private String feedbackId;

    private String action;

    private String remarks;

    private Integer actionBy;

}
