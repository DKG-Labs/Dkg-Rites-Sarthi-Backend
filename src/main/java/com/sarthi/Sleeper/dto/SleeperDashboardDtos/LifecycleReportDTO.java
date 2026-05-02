package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LifecycleReportDTO {

    private String stageName;
    private Integer quantity;
    private LocalDate date;
    private String remarks;


}
