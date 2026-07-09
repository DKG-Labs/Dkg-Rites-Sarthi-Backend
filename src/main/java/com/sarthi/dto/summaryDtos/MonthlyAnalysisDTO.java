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
}