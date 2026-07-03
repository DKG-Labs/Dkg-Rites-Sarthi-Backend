package com.sarthi.Sleeper.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data

public class SleeperWorkflowTransactionDto {

    private Long workflowTransitionId;

    private Long moduleId;

    private Long workflowId;

    private String requestId;

    private String action;

    private String status;

    private String remarks;

    private Long assignedToUser;

    private Long actionBy;

    private LocalDateTime createdDate;

    private String currentRole;
    private String nextRole;
    private String shift;

    private String vendorCode;
    private String plantId;

    private String poiCode;

    private List<Integer> accessibleUserIds;

    private Long createdBy;

    private String rio;
    private Long modifiedBy;

    private String jobStatus;


    private LocalDateTime updatedDate;

    private String productionUnit;

    private String batchNumber;

    private LocalDate castingDate;

    private Integer totalCastedSleepers;

//mix design
    private String mixId;
    private String concreteGrade;
    private String authorityOfApproval;

//plant profile
    private String plantName;
   // private String vendorCode;
    private String plantType;
    private Integer numberOfSheds;

//rawmaterial

    private String rawMaterialType;
    private String supplierName;
    private String approvalReference;
    private LocalDate validFrom;
    private LocalDate validTo;

}
