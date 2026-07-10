package com.sarthi.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "FEEDBACK_WORKFLOW_TRANSITION")
@Data
public class FeedbackWorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FEEDBACK_WORKFLOW_TRANSITION_ID")
    private Long feedbackWorkflowTransitionId;

    @Column(name = "FEEDBACK_ID", nullable = false)
    private String feedbackId;

    @Column(name = "WORKFLOW_ID")
    private Integer workflowId;

    @Column(name = "TRANSITION_ID")
    private Integer transitionId;

    // Product Details
    @Column(name = "PRODUCT_TYPE")
    private String productType; // ERC,SLEEPER,RAILPAD

    @Column(name = "POI_CODE")
    private String poiCode;

    @Column(name = "VENDOR_CODE")
    private String vendorCode;

    @Column(name = "PLANT_ID")
    private String plantId;

    @Column(name = "PO_NUMBER")
    private String poNumber;

    // Role Details
    @Column(name = "CURRENT_ROLE_ID")
    private Integer currentRoleId;

    @Column(name = "CURRENT_ROLE_NAME")
    private String currentRoleName;

    @Column(name = "NEXT_ROLE_ID")
    private Integer nextRoleId;

    @Column(name = "NEXT_ROLE_NAME")
    private String nextRoleName;

    // Workflow Details
    @Column(name = "ACTION")
    private String action;

    @Column(name = "CURRENT_STATUS")
    private String currentStatus;

    @Column(name = "NEXT_STATUS")
    private String nextStatus;

    @Column(name = "REMARKS", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "ASSIGNED_TO_USER")
    private Integer assignedToUser;

    @Column(name = "PROCESS_IE_USER_ID")
    private Integer processIeUserId;

    // Audit Fields
    @Column(name = "CREATED_BY")
    private Integer createdBy;

    @Column(name = "MODIFIED_BY")
    private Integer modifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE")
    private Date createdDate = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MODIFIED_DATE")
    private Date modifiedDate;
}