package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyAnalysisDto {

    private Integer sno;

    private String plantName;
    private String inspectedBy;   // RIO

    private Long production;
    private Long acceptance;

    private Long processRejection;
    private Long finalRejection;

    private Double rejectionPercentage;
}
