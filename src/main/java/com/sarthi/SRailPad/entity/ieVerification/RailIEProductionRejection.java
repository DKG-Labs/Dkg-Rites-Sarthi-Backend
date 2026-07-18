package com.sarthi.SRailPad.entity.ieVerification;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "rail_ie_production_rejection")
public class RailIEProductionRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id", nullable = false)
    private RailIEProductionVerification verification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "info_id")
    private RailIEProductionInfo productionInfo;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "drawing_no")
    private String drawingNo;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "rejected_qty")
    private Integer rejectedQty;

    @Column(name = "reason")
    private String reason;
}
