package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class MoistureSectionDTO {

    private String sectionType; // CA1 / CA2 / FA

    private Double wtWetSample;
    private Double wtDriedSample;
    private Double wtMoistureSample;
    private Double moisturePercent;
    private Double absorptionPercent;
    private Double freeMoisturePercent;
    private Double batchWtDry;
    private Double freeMoistureKg;
    private Double adjustedWeight;
    private Double adoptedWeight;
}