package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "moisture_analysis_entry")
@Data
public class MoistureAnalysisEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private LocalDate entryDate;

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


    private int createdBy;

    private int updatedBy;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private String status;
}

