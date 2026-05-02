package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import java.time.LocalDate;

public interface ProductionProjection {
    Integer getTotalCastedSleepers();
    LocalDate getCastingDate();
    Integer getTotalSleeperTypes();
}
