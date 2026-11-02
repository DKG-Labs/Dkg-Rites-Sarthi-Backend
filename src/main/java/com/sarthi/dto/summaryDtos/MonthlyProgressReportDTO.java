package com.sarthi.dto.summaryDtos;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MonthlyProgressReportDTO {

    private String rly;
    private String poNumber;
    private LocalDateTime poDate;
    private String manufacturer;

    private Double poQty;

    // Unit of measurement of this row: Nos., Set or MT (a PO with two units has one row per unit)
    private String uom;

    private Double monthlyRm;
    private Double monthlyProcess;
    private Double monthlyFinal;

    private Double totalFinalInspected;
    private Double poBalance;
}

