package com.sarthi.SRailPad.dto;

import lombok.Data;

@Data
public class RailCallModificationDto {
    private String callNo;
    private String railPadType;
    private String drawingNo;
    private Integer totalQty;
    private String inspectionDate; // YYYY-MM-DD
    private String updatedBy;
}
