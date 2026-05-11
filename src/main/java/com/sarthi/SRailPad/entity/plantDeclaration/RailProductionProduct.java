package com.sarthi.SRailPad.entity.plantDeclaration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity(name = "RailProductionProduct")
@Table(name = "rail_production_product")
@Data
public class RailProductionProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declaration_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RailProductionDeclaration declaration;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "measurement_mode")
    private String measurementMode; // Pieces, Sets

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RailProductionBatch> batches;
}
