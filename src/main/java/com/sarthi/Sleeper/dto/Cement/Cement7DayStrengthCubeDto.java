package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class Cement7DayStrengthCubeDto {
    private Long id;
    private LocalDate castDate;
    private LocalTime castTime;
    private LocalDate testDate;
    private LocalTime testTime;
    private Double loadKn;
    private Double strengthNmm2;
}
