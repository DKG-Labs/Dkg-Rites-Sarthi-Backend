package com.sarthi.SRailPad.entity.plantDeclaration;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "rail_unit_product")
@Data
public class UnitProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "approval_no")
    private String approvalNo;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "capacity")
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_unit_id")
    private PlantUnit plantUnit;
}
