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

    @Column(name = "production_declaration_id")
    private Long productionDeclarationId;

    @Column(name = "casting_date")
    private LocalDate castingDate;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "shift")
    private String shift;

    @Column(name = "line_no")
    private String lineNo;

    @Column(name = "concrete_grade")
    private String concreteGrade;

    @OneToMany(mappedBy = "declaration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaterCubeSampleDetail> details;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
