package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sleeper_workflow_transaction")
@Data
public class SleeperWorkflowTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer workflowTransitionId;

    private Long workflowId;

    private Long moduleId;

    private String requestId;

    private String action;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    private String currentRole;
    private String nextRole;
    private String shift;

    private String poiCode;

    private String rio;

    private Long assignedToUser;

    private Long createdBy;

    private Long modifiedBy;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

}
