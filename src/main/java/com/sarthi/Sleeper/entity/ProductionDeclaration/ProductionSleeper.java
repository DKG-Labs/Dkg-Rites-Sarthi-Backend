package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;

@Entity
@Table(name="production_sleeper")
public class ProductionSleeper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sleeperNo;

    @ManyToOne
    @JoinColumn(name="bench_group_id")
    private ProductionBenchGroup benchGroup;

}
