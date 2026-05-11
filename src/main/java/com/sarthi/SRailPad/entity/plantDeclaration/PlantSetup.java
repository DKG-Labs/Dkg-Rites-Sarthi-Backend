package com.sarthi.SRailPad.entity.plantDeclaration;

import com.sarthi.SRailPad.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Entity
@Table(name = "rail_plant_setup")
@Data
@EqualsAndHashCode(callSuper = true)
public class PlantSetup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "number_of_units")
    private Integer numberOfUnits;

    @OneToMany(mappedBy = "plantSetup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlantUnit> units;
}
