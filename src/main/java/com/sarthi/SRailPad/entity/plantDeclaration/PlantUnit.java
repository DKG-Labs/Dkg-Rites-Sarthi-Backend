package com.sarthi.SRailPad.entity.plantDeclaration;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "rail_plant_unit")
@Data
public class PlantUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_name")
    private String unitName;

    @Column(name = "address", length = 1000)
    private String address;

    @Column(name = "num_lines")
    private Integer numLines;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_setup_id")
    private PlantSetup plantSetup;

    @OneToMany(mappedBy = "plantUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnitProduct> products;
}
