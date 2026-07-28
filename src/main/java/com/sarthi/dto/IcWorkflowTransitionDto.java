package com.sarthi.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class IcWorkflowTransitionDto {


    private Integer workflowTransitionId;
    private Integer workflowId;
    private Integer transitionId;
    private String requestId;

    private String currentRole;
    private String nextRole;
    private String currentRoleName;
    private String nextRoleName;

    private String status;
    private String action;
    private String remarks;

    private Integer createdBy;
    private Integer modifiedBy;

    private Integer assignedToUser;
    private String jobStatus;

    private Integer processIeUserId;

    private Date createdDate;
    private Integer workflowSequence;

    private String rio;



    private String poNo;
    private String vendorName;
    private String productType;
    private String stage;
    private String inspectionDate;
    private String ibsCaseNo;

    public List<Integer> processIes;


    public List<Integer> finalIes;
}
