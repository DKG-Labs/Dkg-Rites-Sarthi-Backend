package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_declaration")
@Data
public class ProductionDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plantType;
    private String productionUnit;
    private LocalDate castingDate;
    private String shift;

    private String batchNumber;
    private String mixDesignReference;
    private LocalTime lbcTime;

    // ===== SUMMARY SECTION =====

    private Integer totalCastedSleepers;
    private Integer totalSleeperTypes;
    private Double totalRftCasted;

    private String remarks;

    // ===== AUDIT =====

    private Integer createdBy;
    private Integer updatedBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;


    @OneToMany(
            mappedBy = "declaration",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProductionChamber> chambers = new ArrayList<>();
}
