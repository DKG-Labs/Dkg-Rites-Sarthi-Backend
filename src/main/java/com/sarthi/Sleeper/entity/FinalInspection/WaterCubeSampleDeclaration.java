package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "water_cube_sample_declaration")
@Data
public class WaterCubeSampleDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productionDeclarationId;

    private String batchNumber;

    private LocalDate castingDate;

    private String shift;

    private String lineNo;

    private String concreteGrade;

    private Long createdBy;

    private LocalDateTime createdDate;

    private Long updatedBy;

    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaterCubeSampleDetail> details;
}
