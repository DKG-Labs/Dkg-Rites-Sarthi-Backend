package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="production_bench_group")
@Data
public class ProductionBenchGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String benchNo;

    private String sleeperType;

    private Integer mouldPerBench;

    private Double rft;

    private String sleeperCategory;

    private Integer totalSleepers;

    @ManyToOne
    @JoinColumn(name="chamber_id")
    private ProductionStressChamber chamber;

  //  @OneToMany(mappedBy="benchGroup", cascade = CascadeType.ALL)
   // private List<ProductionSleeper> sleepers;

    @org.hibernate.annotations.BatchSize(size = 100)
    @OneToMany(mappedBy="benchGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionSleeper> sleepers = new ArrayList<>();
}