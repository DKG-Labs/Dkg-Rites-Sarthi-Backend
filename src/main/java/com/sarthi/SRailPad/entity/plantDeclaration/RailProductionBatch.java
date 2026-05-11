package com.sarthi.SRailPad.entity.plantDeclaration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity(name = "RailProductionBatch")
@Table(name = "rail_production_batch")
@Data
public class RailProductionBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RailProductionProduct product;

    @Column(name = "batch_no")
    private String batchNo; 

    @Column(name = "comp_abatch")
    private String compABatch; 

    @Column(name = "comp_bbatch")
    private String compBBatch; 
    
    @Column(name = "initial_wt")
    private Double initialWt;

    @Column(name = "final_wt")
    private Double finalWt;

    private Integer quantity;
}
