package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Level4BatchDTO {

    private Integer sno;
    private String batchNo;
    private LocalDate castingDate;

    private Integer totalManufactured;
    private Integer sleeperTypeManufactured;

    private Integer rejected;
    private Integer passed;
}
