package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "mor_test_result")
@Data
public class MorTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mor_sample_id")
    private MorSampleDeclaration declaration;

    @Column(name = "bench_number")
    private String benchNumber;

    @Column(name = "sleeper_no")
    private String sleeperNo;

    @Column(name = "ct_kn")
    private Double ctKn;

    @Column(name = "cb_kn")
    private Double cbKn;

    @Column(name = "rs_kn")
    private Double rsKn;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "load_kn")
    private Double loadKn;

    @Column(name = "strength")
    private Double strength;

    @Column(name = "result")
    private String result; // Pass, Fail

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "is_pass")
    private Boolean isPass;

    @Column(name = "testing_date")
    private LocalDate testDate;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_date")
    private java.time.LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_date")
    private java.time.LocalDateTime updatedDate;
}
