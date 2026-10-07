package com.sarthi.SRailPad.dto;

import lombok.Data;

@Data
public class RailUnblockReqDto {
    private Long unblockedBy;
    private String unblockedByName;
    private String unblockedByRole;
    private String remarks;
}
