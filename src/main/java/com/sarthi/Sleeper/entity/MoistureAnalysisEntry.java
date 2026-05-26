package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "moisture_analysis_entry")
@Data
public class MoistureAnalysisEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "entry_date")
    private LocalDate entryDate;

    private String shift;

    @Column(name = "entry_time")
    private String entryTime;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "approved_mix_design")
    private String approvedMixDesign;

    // ===== DESIGN =====
    @Column(name = "design_ac")
    private Double designAC;

    @Column(name = "design_wc")
    private Double designWC;

    @Column(name = "design_cement")
    private Double designCement;

    @Column(name = "design_ca1")
    private Double designCA1;

    @Column(name = "design_ca2")
    private Double designCA2;

    @Column(name = "design_fa")
    private Double designFA;

    @Column(name = "design_water")
    private Double designWater;

    @Column(name = "design_admix")
    private Double designAdmix;

    // ===== ACTUAL =====
    @Column(name = "actual_cement")
    private Double actualCement;

    @Column(name = "actual_ca1")
    private Double actualCA1;

    @Column(name = "actual_ca2")
    private Double actualCA2;

    @Column(name = "actual_fa")
    private Double actualFA;

    @Column(name = "actual_water")
    private Double actualWater;

    @Column(name = "actual_admix")
    private Double actualAdmix;

    // ===== COMMON =====
    @Column(name = "wt_adopted_ca1")
    private Double wtAdoptedCa1;

    @Column(name = "wt_adopted_ca2")
    private Double wtAdoptedCa2;

    @Column(name = "wt_adopted_fa")
    private Double wtAdoptedFa;

    @Column(name = "total_free_moisture")
    private Double totalFreeMoisture;

    @Column(name = "adjusted_water_wt")
    private Double adjustedWaterWt;

    @Column(name = "wc_ratio")
    private Double wcRatio;

    @Column(name = "ac_ratio")
    private Double acRatio;

    @Column(name = "created_by")
    private int createdBy;

    @Column(name = "updated_by")
    private int updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    private String status;

    private String plantId;
    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoistureSection> sections;
}

