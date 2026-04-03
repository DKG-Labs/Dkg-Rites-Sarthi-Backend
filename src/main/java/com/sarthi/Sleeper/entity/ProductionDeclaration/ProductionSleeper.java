package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="production_sleeper")
@Data
public class ProductionSleeper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sleeperNo;

    @ManyToOne
    @JoinColumn(name="bench_group_id")
    private ProductionBenchGroup benchGroup;
    @ManyToOne
    @JoinColumn(name = "gang_id")
    private ProductionLongLineGang gang;
}
