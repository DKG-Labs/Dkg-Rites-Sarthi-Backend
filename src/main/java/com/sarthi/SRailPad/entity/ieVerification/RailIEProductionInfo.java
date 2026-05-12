package com.sarthi.SRailPad.entity.ieVerification;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "rail_ie_production_info")
public class RailIEProductionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id", nullable = false)
    private RailIEProductionVerification verification;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "initial_wt")
    private Double initialWt;

    @Column(name = "final_wt")
    private Double finalWt;

    @Column(name = "quantity_produced")
    private Integer quantityProduced;
}
