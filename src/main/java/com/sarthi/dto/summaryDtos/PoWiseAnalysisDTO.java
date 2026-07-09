package com.sarthi.dto.summaryDtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PoWiseAnalysisDTO {

    private String rlyZone;
    private String poNumber;
    private LocalDate poDate;
    private Double poQty;

    private Double manufactured;
    private Double inspected;
    private Double rejected;

    private Double rmRejPercent;
    private Double processRejPercent;
    private Double finalRejPercent;
}
