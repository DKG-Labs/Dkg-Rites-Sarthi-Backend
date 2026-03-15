package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalTime;

@Data
public class CementNormalConsistencyObservationDto {
    private Double percentWaterAdded;
    private Double volume;
    private LocalTime timeOfAdding;
    private LocalTime readingTime;
    private Double needleReading;
}
