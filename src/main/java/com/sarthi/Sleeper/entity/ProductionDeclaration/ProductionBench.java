package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "production_bench")
@Data
public class ProductionBench {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String benchNo;          // 10A, 2A, etc
    private String sleeperType;
    private Integer mouldPerBench;
    private Double rftMeters;

    private Integer count;

    private Integer createdBy;
    private Integer updatedBy;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamber_id")
    private ProductionChamber chamber;
}