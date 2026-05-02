package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import java.time.LocalDate;

public interface FinalInspectionProjection {
    LocalDate getTestDate();
    Integer getRejectedCount();
}
