package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "raw_material_consumption")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialConsumption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_use")
    private String dateOfUse;

    @Column(name = "raw_material")
    private String rawMaterial;

    @Column(name = "sub_type")
    private String subType;

    @Column(name = "used_for")
    private String usedFor;

    @Column(name = "sleepers_made")
    private Integer sleepersMade;

    @Column(name = "estimated_qty")
    private Double estimatedQty;

    @Column(name = "actual_qty")
    private Double actualQty;

    @Column(name = "status")
    private String status;
}
