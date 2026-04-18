package com.sarthi.Sleeper.entity;

import com.sarthi.Sleeper.entity.FinalInspection.MorSampleDeclaration;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mor_test_result")
@Data
public class MorTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate testingDate;

    private Double weight;

    private Double loadKn;

    private Double strength;

    private String result;

    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;

    private String shift;
    private String vendorCode;
    private String plantId;

    private LocalDate samplingDate;

    private String concreteGrade;

    private String sampleIdentificationNumber;


    @ManyToOne
    @JoinColumn(name = "mor_sample_id")
    private MorSampleDeclaration morSample;
}
