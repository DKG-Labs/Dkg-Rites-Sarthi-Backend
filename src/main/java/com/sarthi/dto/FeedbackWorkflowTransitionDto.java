package com.sarthi.dto;

import lombok.Data;

import java.util.Date;

@Data
public class FeedbackWorkflowTransitionDto {

    private Long feedbackWorkflowTransitionId;

    private String feedbackId;

    private Integer workflowId;

    private Integer transitionId;

    private String productType;

    private String poiCode;

    private String vendorCode;

    private String plantId;

    private Integer currentRoleId;

    private String currentRoleName;

    private Integer nextRoleId;

    private String nextRoleName;

    private String action;

    private String currentStatus;

    private String nextStatus;

    private String remarks;

    private Integer assignedToUser;

    private Integer processIeUserId;

    private Integer createdBy;

    private Integer modifiedBy;

    private Date createdDate;

    private Date modifiedDate;
}