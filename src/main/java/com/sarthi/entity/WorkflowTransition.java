package com.sarthi.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "WORKFLOW_TRANSITION")
@Data
public class WorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WORKFLOWTRANSITIONID")
    private Integer workflowTransitionId;

    @Column(name = "WORKFLOWID", nullable = false)
    private Integer workflowId;

    @Column(name = "TRANSITIONID", nullable = false)
    private Integer transitionId;

    @Column(name = "REQUESTID", nullable = false)
    private String requestId; // e.g. IC/2025/00123

    @Column(name = "CURRENTROLE")
    private String currentRole;

    @Column(name = "NEXTROLE")
    private String nextRole;

    @Column(name = "current_role_name")
    private String currentRoleName;

    @Column(name = "next_role_name")
    private String nextRoleName;

    @Column(name = "STATUS")
    private String status; // SUBMITTED, VERIFIED, RETURNED, ROUTED_CORRECTION

    @Column(name = "ACTION")
    private String action;
    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "CREATEDBY")
    private Integer createdBy;

    private Integer modifiedBy;

    @Column(name = "assigned_to_user")
    private Integer assignedToUser;

    @Column(name = "job_status")
    private String jobStatus;

    @Column(name = "process_ie_user_id")
    private Integer processIeUserId;

    @Column(name = "CREATEDDATE")
    private Date createdDate = new Date();

    @Column(name = "WORKFLOWSEQUENCE")
    private Integer workflowSequence;

    private String rio;

    @Column(name = "SWIFT_CODE")
    private String swiftCode;

    @Column(name = "IS_PRIMARY_SWIFT")
    private Boolean primarySwift;

    @PrePersist
    @PreUpdate
    public void ensureAssignedToUserNullOnCreated() {
        if ("Created".equalsIgnoreCase(this.status) || "CREATED".equalsIgnoreCase(this.status)) {
            this.assignedToUser = null;
        }
    }

}

