package com.sarthi.Sleeper.dto.BenchMouldLongStrssDtos;

import lombok.Data;

@Data
public class BMDetailResponseDTO {

    private Long id;

    private String sleeperCode;
    private String sleeperDrawingNo;

    private String declarationMode;

    private Integer benchFrom;
    private Integer benchTo;
    private Integer benchNumber;

    private Integer gangFrom;
    private Integer gangTo;
    private Integer gangNumber;

    private Integer noOfMoulds;
}