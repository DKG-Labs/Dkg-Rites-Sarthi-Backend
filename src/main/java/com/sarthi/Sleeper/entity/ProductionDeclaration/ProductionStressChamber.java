package com.sarthi.Sleeper.entity.ProductionDeclaration;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="production_stress_chamber")
public class ProductionStressChamber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer chamberNo;

    @ManyToOne
    @JoinColumn(name="declaration_id")
    private ProductionDeclaration declaration;

    @OneToMany(mappedBy="chamber", cascade = CascadeType.ALL)
    private List<ProductionBenchGroup> benchGroups;

}