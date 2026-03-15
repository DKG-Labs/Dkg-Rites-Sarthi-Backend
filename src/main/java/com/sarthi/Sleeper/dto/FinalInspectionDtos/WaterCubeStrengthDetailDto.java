package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

@Data
public class WaterCubeStrengthDetailDto {
    private Integer sampleNumber;
    private Integer cubeIndex;
    private String cubeId;
    private Double weightKg;
    private Double loadKn;
    private Double strengthNmm2;
    private String testingDate; // Format: YYYY-MM-DD
    private String testingTime; // Format: HH:mm
}
