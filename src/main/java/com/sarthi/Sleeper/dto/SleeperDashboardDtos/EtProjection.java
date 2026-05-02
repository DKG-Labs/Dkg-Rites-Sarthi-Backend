package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import java.time.LocalDateTime;

public interface EtProjection {
    LocalDateTime getCreatedDate();
    Integer getSleeperCount();
}
