package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "WORKFLOW_DELETE_HISTORY")
@Data
public class WorkflowDeleteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Request information
    @Column(name = "REQUEST_ID")
    private String requestId;

    @Column(name = "REQUEST_TYPE")
    private String requestType;

    // Workflow information
    @Column(name = "WORKFLOW_TRANSITION_ID")
    private Integer workflowTransitionId;

    @Column(name = "WORKFLOW_ID")
    private Integer workflowId;

    @Column(name = "TRANSITION_ID")
    private Integer transitionId;

    @Column(name = "CURRENT_ROLE")
    private String currentRole;

    @Column(name = "NEXT_ROLE")
    private String nextRole;

    @Column(name = "CURRENT_ROLE_NAME")
    private String currentRoleName;

    @Column(name = "NEXT_ROLE_NAME")
    private String nextRoleName;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ACTION")
    private String action;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "CREATED_BY")
    private Integer createdBy;

    @Column(name = "MODIFIED_BY")
    private Integer modifiedBy;

    @Column(name = "ASSIGNED_TO_USER")
    private Integer assignedToUser;

    @Column(name = "JOB_STATUS")
    private String jobStatus;

    @Column(name = "PROCESS_IE_USER_ID")
    private Integer processIeUserId;

    @Column(name = "WORKFLOW_SEQUENCE")
    private Integer workflowSequence;

    @Column(name = "RIO")
    private String rio;

    @Column(name = "SWIFT_CODE")
    private String swiftCode;

    @Column(name = "IS_PRIMARY_SWIFT")
    private Boolean primarySwift;

    @Column(name = "TRANSITION_CREATED_DATE")
    private Date transitionCreatedDate;

    // Inspection details
    @Column(name = "PO_NO")
    private String poNo;

    @Column(name = "CERTIFICATE_NO")
    private String certificateNo;

    @Column(name = "INSPECTION_CREATED_ON")
    private LocalDateTime  inspectionCreatedOn;

    // Audit information
    @Column(name = "DELETED_BY")
    private Integer deletedBy;

    @Column(name = "DELETED_ON")
    private Date deletedOn;
}