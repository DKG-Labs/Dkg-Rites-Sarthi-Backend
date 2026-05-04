package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Level1DTO {

    private int sno;
    private String rly;
    private String poNo;
    private LocalDate poDate;
    private String vendor;
    private String region;

    private Integer poQty;
    private Integer accQty;
    private Integer balQty;

    private Double rejectionPercent;
}