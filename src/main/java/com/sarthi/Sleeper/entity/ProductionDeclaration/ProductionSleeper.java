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
    @Column(name = "sleeper_type")
    private String sleeperType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="bench_group_id")
    private ProductionBenchGroup benchGroup;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gang_id")
    private ProductionLongLineGang gang;
}
