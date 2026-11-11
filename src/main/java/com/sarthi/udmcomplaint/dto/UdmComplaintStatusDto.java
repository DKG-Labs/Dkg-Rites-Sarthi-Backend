package com.sarthi.udmcomplaint.dto;

import lombok.Data;

/** Complaint state returned after an action and on the CM list. */
@Data
public class UdmComplaintStatusDto {
    private Long udmRejectionId;
    private String rejectionNumber;
    private String complaintStatus;
    private String pendingWith;
    private String assignedRio;
    private Integer assignedCmUserId;
    private String assignedCmName;
    private String lastRemarks;
    private String closedAt;
    private String updatedAt;
}
