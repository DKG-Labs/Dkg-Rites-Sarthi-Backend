package com.sarthi.dto.summaryDtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LotWiseClosedLoopDTO {

    private LocalDate inspectionDate;
    private String shift;

    private Double accepted;
    private Double rejected;

    private Double shearing;
    private Double turning;
    private Double mpi;
    private Double forging;
    private Double quenching;
    private Double tempering;
    private Double testing;
}
