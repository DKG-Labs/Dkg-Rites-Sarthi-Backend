package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

@Data
public class WaterCubeStrengthDetailRequestDto {
    private Integer sampleNumber;
    private Integer cubeIndex;
    private String cubeId;
    private Double weightKg;
    private Double loadKn;
    private Double strengthNmm2;
    private String testingDate;
    private String testingTime;
}
