package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Monthly Analysis of Units - Process Inspection (Sleeper): one row per PSC sleeper plant.
 * "fy*" = cumulative for the current financial year (1 April -> today);
 * "month*" = the selected period ("During the Current Month").
 * Inspected = sleepers cast; Acceptance = Inspected - (Process + Final rejection).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyAnalysisDto {

    private Integer sno;

    private String zonalRailway;
    private String plantId;
    private String plantName;
    private String rio;

    private Long fyInspected;
    private Long fyAcceptance;
    private Long fyProcessRejection;
    private Long fyFinalRejection;
    private Double fyRejectionPercentage;

    private Long monthInspected;
    private Long monthAcceptance;
    private Long monthProcessRejection;
    private Long monthFinalRejection;
    private Double monthRejectionPercentage;
}
