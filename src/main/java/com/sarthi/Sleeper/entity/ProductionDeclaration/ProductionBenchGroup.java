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

    private Integer benchNo;

    private String sleeperType;

    private Integer mouldPerBench;

    private Double rft;

    @ManyToOne
    @JoinColumn(name="chamber_id")
    private ProductionStressChamber chamber;

  //  @OneToMany(mappedBy="benchGroup", cascade = CascadeType.ALL)
   // private List<ProductionSleeper> sleepers;

    @OneToMany(mappedBy="benchGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionSleeper> sleepers = new ArrayList<>();
}