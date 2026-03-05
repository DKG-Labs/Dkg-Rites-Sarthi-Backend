package com.sarthi.Sleeper.dto;

import jdk.jshell.Snippet;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SleeperWorkflowTransactionDto {

    private Long id;

    private Long moduleId;

    private Long workflowId;

    private String requestId;

    private String action;

    private String status;

    private String remarks;

    private Long assignedToUser;

    private Long actionBy;

    private LocalDateTime createdDate;


}
