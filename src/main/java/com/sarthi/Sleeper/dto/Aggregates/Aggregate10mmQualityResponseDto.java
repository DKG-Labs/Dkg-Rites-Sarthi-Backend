package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Aggregate10mmQualityResponseDto {
    private Long id;
    private LocalDate testDate;
    private String typeOfTesting;
    private String consignmentNo;

    // Section 1: Crushing
    private Double crushingMouldWt;
    private Double crushingMouldSampleWt;
    private Double crushingSampleWt;
    private Double crushingPassingWt;
    private Double crushingValue;
    private String crushingResult;

    // Section 2: Impact
    private Double impactMouldWt;
    private Double impactMouldSampleWt;
    private Double impactSampleWt;
    private Double impactPassingWt;
    private Double impactValue;
    private String impactResult;

    // Section 3: Abrasion
    private Double abrasionSampleWt;
    private Double abrasionPassingWt;
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
