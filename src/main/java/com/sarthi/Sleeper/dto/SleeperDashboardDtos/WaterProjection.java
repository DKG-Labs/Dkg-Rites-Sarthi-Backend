package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import java.time.LocalDateTime;

public interface WaterProjection {
    LocalDateTime getCreatedDate();
    Double getAvgStrength();
}
