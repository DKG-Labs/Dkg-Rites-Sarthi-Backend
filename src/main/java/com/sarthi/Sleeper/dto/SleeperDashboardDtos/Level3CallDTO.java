package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Level3CallDTO {

    private Integer sno;
    private String callNo;
    private LocalDate desDate;

    private Integer offered;
    private Integer accepted;
    private Integer rejected;

    private Double rejectionPercentage;

    private String icNo;
}