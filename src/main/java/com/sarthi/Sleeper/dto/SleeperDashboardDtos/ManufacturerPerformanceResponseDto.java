package com.sarthi.Sleeper.dto.SleeperDashboardDtos;


import lombok.Data;

import java.util.List;

@Data
public class ManufacturerPerformanceResponseDto {

    private List<MonthlyPerformanceDto> monthlyPerformance;

    private Long totalInspected;

    private Long totalRejected;

    private Double averageRejectionPercentage;
}
