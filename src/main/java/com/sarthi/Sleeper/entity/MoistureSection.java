package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "moisture_section")
@Data
public class MoistureSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "section_type")
    private String sectionType;

    @Column(name = "wt_wet_sample")
    private Double wtWetSample;

    @Column(name = "wt_dried_sample")
    private Double wtDriedSample;

    @Column(name = "wt_moisture_sample")
    private Double wtMoistureSample;

    @Column(name = "moisture_percent")
    private Double moisturePercent;

    @Column(name = "absorption_percent")
    private Double absorptionPercent;

    @Column(name = "free_moisture_percent")
    private Double freeMoisturePercent;

    @Column(name = "batch_wt_dry")
    private Double batchWtDry;

    @Column(name = "free_moisture_kg")
    private Double freeMoistureKg;

    @Column(name = "adjusted_weight")
    private Double adjustedWeight;

    @Column(name = "adopted_weight")
    private Double adoptedWeight;


    @ManyToOne
    @JoinColumn(name = "entry_id")
    private MoistureAnalysisEntry entry;
}
