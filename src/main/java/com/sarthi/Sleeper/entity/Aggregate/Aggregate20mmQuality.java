package com.sarthi.Sleeper.entity.Aggregate;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aggregate_20mm_quality")
public class Aggregate20mmQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_of_testing")
    private String typeOfTesting;
    
    @Column(name="request_id")
    private Long requestId;

    private LocalDate testDate;
    private String consignmentNo;

    // Section 1: Crushing
    private Double crushingMouldWt;
    private Double crushingMouldSampleWt;
    private Double crushingSampleWt;
    private Double crushingPassingWt; // passing 3.35mm
    private Double crushingValue;
    private String crushingResult;

    // Section 2: Impact
    private Double impactMouldWt;
    private Double impactMouldSampleWt;
    private Double impactSampleWt;
    private Double impactPassingWt; // passing 2.36mm
    private Double impactValue;
    private String impactResult;

    // Section 3: Abrasion
    private Double abrasionSampleWt;
    private Double abrasionPassingWt; // passing 1.7mm
    private Double abrasionValue;
    private String abrasionResult;

    // Session Context
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;

    // Audit Fields
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
}
