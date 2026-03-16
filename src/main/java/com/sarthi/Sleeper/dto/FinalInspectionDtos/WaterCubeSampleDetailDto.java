package com.sarthi.Sleeper.dto.FinalInspectionDtos;

import lombok.Data;

@Data
public class WaterCubeSampleDetailDto {

    private Long id;

    private Integer sampleNumber;

    private Integer cubeNumber;

    private String benchNumber;

    private String sequence;
}
