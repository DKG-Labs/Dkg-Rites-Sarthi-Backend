package com.sarthi.Sleeper.entity.FinalInspection;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "mor_sample_declaration")
@Data
public class MorSampleDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sampling_date")
    private LocalDate samplingDate;

    @Column(name = "shift")
    private String shift;

    @Column(name = "line_no")
    private String lineNo;

    @Column(name = "concrete_grade")
    private String concreteGrade;

    @Column(name = "plant_type")
    private String plantType;

    @Column(name = "shed_line")
    private String shedLine;

    @Column(name = "sample_identification_number")
    private String sampleIdentificationNumber;

    @Column(name = "water_cube_strength_test_id")
    private Long waterCubeStrengthTestId;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "casting_date")
    private LocalDate castingDate;

    @Column(name = "mr_samples_required")
    private Integer mrSamplesRequired;

    @Column(name = "mr_test_type")
    private String mrTestType; // Fresh, Retest

    @Column(name = "test_status")
    private String status; // PENDING_TEST, COMPLETED

    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL)
    private List<MorSampleDetail> details;

    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL)
    private List<MorTestResult> testResults;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
