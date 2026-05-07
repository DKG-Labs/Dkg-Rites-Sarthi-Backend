package com.sarthi.Sleeper.dto.SleeperDashboardDtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPerformanceDto {

    private String month;

    private Long inspectedNos;

    private Long rejectedNos;

    private Double rejectionPercentage;
}
