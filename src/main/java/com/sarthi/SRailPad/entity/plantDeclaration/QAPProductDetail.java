package com.sarthi.SRailPad.entity.plantDeclaration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "RailQAPProductDetail")
@Table(name = "rail_qap_product_detail")
@Data
public class QAPProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qap_id")
    @JsonIgnore
    private ApprovedQAP approvedQAP;

    @Column(name = "pad_type")
    private String padType;

    // Mixing Parameters
    @Column(name = "min_mixing_time")
    private Double minMixingTime;

    @Column(name = "max_mixing_time")
    private Double maxMixingTime;

    @Column(name = "min_mixing_temp")
    private Double minMixingTemp;

    @Column(name = "max_mixing_temp")
    private Double maxMixingTemp;

    @Column(name = "mixing_weight")
    private Double mixingWeight;

    // Moulding Parameters
    @Column(name = "min_curing_time")
    private Double minCuringTime;

    @Column(name = "max_curing_time")
    private Double maxCuringTime;

    @Column(name = "min_curing_temp")
    private Double minCuringTemp;

    @Column(name = "max_curing_temp")
    private Double maxCuringTemp;

    @Column(name = "min_curing_pressure")
    private Double minCuringPressure;

    @Column(name = "max_curing_pressure")
    private Double maxCuringPressure;
}
