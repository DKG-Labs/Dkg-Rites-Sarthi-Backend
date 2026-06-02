package com.sarthi.Sleeper.dto.SleeperDashboardDtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftWiseProductionReportDto {

    private String date;

    private String shift;

    private String lineOrShedNo;

    private Long noOfBatches;

    private Integer noOfSleepers;

    private String sleeperTypesAndCounts;

    private Long processRejectedSleepers;

    private Long finalRejectedSleepers;

    private Long etRejectedSleepers;


}