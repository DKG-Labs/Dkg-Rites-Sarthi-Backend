package com.sarthi.SRailPad.dto;

import lombok.Data;

@Data
public class RailCallModificationDto {
    private String callNo;
    private String railPadType;
    private String drawingNo;
    private String ncrgrspType;
    private Integer totalQty;
    private Integer noOfSets;
    private Integer noOfLots;
    private String processIcNo;
    private String processInspectionCertNo;
    private String inspectionDate; // YYYY-MM-DD
    private String updatedBy;
    private String remarks;
    private java.util.List<com.sarthi.SRailPad.entity.inspectionCall.RailInspectionLot> lots;
}
