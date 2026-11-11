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
    private String actionDate;
}
