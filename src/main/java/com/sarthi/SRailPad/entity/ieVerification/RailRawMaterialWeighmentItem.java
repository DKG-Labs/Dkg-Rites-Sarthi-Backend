package com.sarthi.SRailPad.entity.ieVerification;

import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "rail_raw_material_weighment_item")
public class RailRawMaterialWeighmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weighment_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RailRawMaterialWeighment weighment;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "weight", nullable = false)
    private Double weight;
}
