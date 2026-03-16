package com.sarthi.Sleeper.entity.FinalInspection;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mor_sample_declaration")
@Data
public class MorSampleDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate samplingDate;

    private String concreteGrade;

    private String plantType;

    private String shedLine;

    private String sampleIdentificationNumber;

    private Long createdBy;

    private LocalDateTime createdDate;

    private Long updatedBy;

    private LocalDateTime updatedDate;
}
