package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bench_mould_inspection")
@Data
public class BenchMouldInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lineShedNo;

    private LocalDate checkingDate;

    private String benchGangNo;

    private String sleeperType;

    private LocalDate latestCastingDate;

    // Bench Inspection
    private String benchVisualResult;
    private String benchDimensionalResult;

    // Mould Inspection
    private String mouldVisualResult;
    private String mouldDimensionalResult;

    private String combinedRemarks;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    private String status;
}
