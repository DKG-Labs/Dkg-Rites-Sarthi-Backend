package com.sarthi.Sleeper.dto;

import lombok.Data;

@Data
public class MoistureAnalysisRequestDTO {

    private String entryDate;      // yyyy-MM-dd
    private String shift;
    private String entryTime;      // HH:mm
    private String batchNo;

    private Double batchWtDryCa1;
    private Double batchWtDryCa2;
    private Double batchWtDryFa;
    private Double batchWtDryWater;
    private Double batchWtDryAdmix;
    private Double batchWtDryCement;

    private Double wtAdoptedCa1;
    private Double wtAdoptedCa2;
    private Double wtAdoptedFa;

    private Double totalFreeMoisture;
    private Double adjustedWaterWt;
    private Double wcRatio;
    private Double acRatio;


    /* Section */

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


    /* Audit */

    private int createdBy;
    private int updatedBy;
}

