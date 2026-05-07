package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParetoAnalysisDto {

    private String defectCategory;

    private Long defectCount;

    private Double percentage;

    private Double cumulativePercentage;
}
