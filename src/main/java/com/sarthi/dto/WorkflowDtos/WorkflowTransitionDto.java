package com.sarthi.dto.WorkflowDtos;

import lombok.Data;

import jakarta.persistence.Column;
import java.util.Date;
import java.util.List;

@Data
public class WorkflowTransitionDto {

    private Integer workflowTransitionId;
    private Integer workflowId;
    private Integer transitionId;

    private String requestId;

    private String status;      // SUBMITTED / VERIFIED / RETURNED / ROUTED_CORRECTION
    private String action;      // VERIFY / RETURN_TO_VENDOR / FIX_ROUTING
    private String remarks;

    private String currentRole;
    private String nextRole;

    private Integer createdBy;
    private Date createdDate;

    private Integer transitionOrder;
    private Integer workflowSequence;
    private Integer assignedToUser;

    private Integer modifiedBy;

    private String poNo;
    private String rawPoNo;
    private String vendorName;
    private String productType;
    private String desiredInspectionDate;

    // Inspection date range (start - end)
    private String inspectionDate;

    private String rio;

    private List<Integer> processIes;

    private List<Integer> finalIes;

}
