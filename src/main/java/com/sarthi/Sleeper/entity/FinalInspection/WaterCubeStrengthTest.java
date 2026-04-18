package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "water_cube_strength_test")
@Data
public class WaterCubeStrengthTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "water_cube_sample_declaration_id")
    private WaterCubeSampleDeclaration waterCubeSampleDeclaration;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "concrete_grade")
    private String concreteGrade;

    @Column(name = "casting_date")
    private String castingDate;

    @Column(name = "shift")
    private String shift;

    @Column(name = "line_no")
    private String lineNo;

    @Column(name = "fck_target")
    private Double fckTarget;

    @Column(name = "age_days")
    private Integer ageDays;

    @Column(name = "s1_avg")
    private Double s1Avg;

    @Column(name = "s2_avg")
    private Double s2Avg;

    @Column(name = "avg_x")
    private Double avgX;

    @Column(name = "min_y")
    private Double minY;

    @Column(name = "s1_variation")
    private Double s1Variation;

    @Column(name = "s2_variation")
    private Double s2Variation;

    @Column(name = "condition1")
    private Boolean condition1;

    @Column(name = "condition2")
    private Boolean condition2;

    @Column(name = "condition3")
    private Boolean condition3;

    @Column(name = "mr_samples_required")
    private Integer mrSamplesRequired;

    @Column(name = "final_test_result")
    private String finalTestResult;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Long updatedBy;


    private String vendorCode;
    private String plantId;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "strengthTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaterCubeStrengthDetail> details;
}
