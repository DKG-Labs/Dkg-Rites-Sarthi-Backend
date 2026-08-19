package com.sarthi.SRailPad.dto;

import lombok.Data;

@Data
public class RailWithdrawRequestDto {
    private Long workflowTransitionId;
    private String requestId;
    private String callNo;
    private String withdrawnBy;
    private String actionBy;
    private String remarks;
}
