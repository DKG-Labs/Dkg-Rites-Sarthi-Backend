package com.sarthi.Sleeper.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MoistureAnalysisResponseDTO {

        private Long id;

        private String entryDate;
        private String shift;
        private String entryTime;
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

        private String sectionType;

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

        private int createdBy;
        private int updatedBy;

        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;

        private String status;

}
