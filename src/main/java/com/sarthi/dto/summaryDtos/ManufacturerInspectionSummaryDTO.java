package com.sarthi.dto.summaryDtos;

import lombok.Data;

@Data
public class ManufacturerInspectionSummaryDTO {

    private Long manufacturerId;
    private String manufacturerName;
    private String poiCode;
    private String rio;        // NEW
    private String username;

    private String stage; // RAW / PROCESS / FINAL

    private Double inspectedQty;
    private Double acceptedQty;
    private Double rejectedQty;

    private Double rejectionPercentage;
}