package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "modulus_of_failure")
@Data
public class ModulusOfFailure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate samplingDate;

    private String concreteGrade;

    private String plantType;

    private String shedLineNumber;

    private String batchNo;

    private LocalDate castingDate;

    private String benchGangNumber;

    private String mouldNo;

    private String sampleIdentification;

    private String mrResult;

    private String sampleType;

    private String shift;
    private String vendorCode;
    private String plantId;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;
}
