package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalTime;

@Data
public class CementSettingTimeObservationDto {
    private LocalTime readingTime;
    private Double needlePenetration;
    private String finalSpot;
}
