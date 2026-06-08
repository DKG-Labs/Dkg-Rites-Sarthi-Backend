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

    private Double monthlyRm;
    private Double monthlyProcess;
    private Double monthlyFinal;

    private Double totalFinalInspected;
    private Double poBalance;
}

