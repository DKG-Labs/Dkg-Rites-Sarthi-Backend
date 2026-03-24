package com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos;

import lombok.Data;

@Data
public class BMDetailRequestDTO {

    // Turnout fields (optional)
    private Long id;
    private String sleeperCode;
    private String sleeperDrawingNo;

    private String declarationMode; // SINGLE / RANGE

    // STRESS
    private Integer benchFrom;
    private Integer benchTo;
    private Integer benchNumber;

    // LONG LINE
    private Integer gangFrom;
    private Integer gangTo;
    private Integer gangNumber;

    private Integer noOfMoulds;
}