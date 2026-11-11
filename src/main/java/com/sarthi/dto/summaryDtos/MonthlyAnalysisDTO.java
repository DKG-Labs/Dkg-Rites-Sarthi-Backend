package com.sarthi.dto.summaryDtos;

import lombok.Data;

@Data
public class MonthlyAnalysisDTO {

    private String manufacturer;

    private String rio;
    private Double manufactured;
    private Double inspected;
    private Double rejected;

    private Double rmRejected;
    private Double processRejected;
    private Double finalRejected;

    private Double rmRejPercent;
    private Double processRejPercent;
    private Double finalRejPercent;

    private Long noOfPos;
    private Double poQty;
    private String uom;
    private String vendorCode;

    // Process Inspection (Process ICs issued in selected range)
    private Double processInspected;
    private Double processAccepted;
    private Double processRejectedQty;
    private Double processRejectionPct;
    // Cumulative for current FY (1 April to today)
    private Double fyInspected;
    private Double fyAccepted;
    private Double fyRejected;
    private Double fyRejectionPct;
}