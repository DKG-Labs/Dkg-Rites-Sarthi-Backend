package com.sarthi.udmcomplaint.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** Audit trail / timeline of every UDM complaint action (kept apart from WORKFLOW_TRANSITION). */
@Entity
@Table(name = "udm_complaint_workflow_transition")
@Data
public class UdmComplaintWorkflowTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "udm_rejection_id", nullable = false)
    private Long udmRejectionId;

    @Column(name = "rejection_number", nullable = false, length = 100)
    private String rejectionNumber;

    @Column(name = "workflow_id", nullable = false)
    private Integer workflowId;

    @Column(name = "transition_id", nullable = false)
    private Integer transitionId;

    @Column(name = "action", nullable = false, length = 60)
    private String action;

    @Column(name = "current_role_id")
    private Integer currentRoleId;

    @Column(name = "current_role_name", length = 100)
    private String currentRoleName;

    @Column(name = "next_role_id")
    private Integer nextRoleId;

    @Column(name = "next_role_name", length = 100)
    private String nextRoleName;

    @Column(name = "current_status", length = 40)
    private String currentStatus;

    @Column(name = "next_status", length = 40)
    private String nextStatus;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "assigned_rio", length = 50)
    private String assignedRio;

    @Column(name = "assigned_to_user")
    private Integer assignedToUser;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}
