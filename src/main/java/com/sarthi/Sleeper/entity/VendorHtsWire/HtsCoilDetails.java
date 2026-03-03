package com.sarthi.Sleeper.entity.VendorHtsWire;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "hts_coil_details")
@Data
public class HtsCoilDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coil_from")
    private String coilFrom;   // example: 22

    @Column(name = "coil_to")
    private String coilTo;     // example: 23 (null for single)

    @Column(name = "lot_no")
    private String lotNo;
    @Column(name = "coil_no")
    private String coilNo;

    @Column(name = "qty_kg")
    private Double qtyKg;

    @Column(name = "entry_type")
    private String entryType;  // SINGLE / RANGE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hts_wire_id")
    private HtsWire htsWire;
}