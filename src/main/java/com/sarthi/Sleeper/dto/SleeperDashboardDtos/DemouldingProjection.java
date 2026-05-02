package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import java.time.LocalDate;

public interface DemouldingProjection {
    LocalDate getInspectionDate();
    Integer getRejectedCount();
}