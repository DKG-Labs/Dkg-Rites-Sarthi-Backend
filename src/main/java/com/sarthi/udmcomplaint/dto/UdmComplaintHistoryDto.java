package com.sarthi.udmcomplaint.dto;

import lombok.Data;

@Data
public class UdmComplaintHistoryDto {
    private String action;
    private String fromRole;
    private String toRole;
    private String status;
    private String remarks;
    private String assignedRio;
    private Integer assignedToUser;
    private Integer actionBy;
    /** Name and employee code of the user who performed the step (from user_master). */
    private String actionByName;
    private String actionByEmployeeCode;
    private String actionDate;
}
