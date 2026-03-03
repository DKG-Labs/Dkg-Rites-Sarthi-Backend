package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_chamber")
@Data
public class ProductionChamber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chamberNo;

    private Integer createdBy;
    private Integer updatedBy;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declaration_id")
    private ProductionDeclaration declaration;


    @OneToMany(
            mappedBy = "chamber",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProductionBench> benches = new ArrayList<>();
}