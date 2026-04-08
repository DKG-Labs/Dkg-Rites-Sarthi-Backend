package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mix_design")
@Data
public class MixDesign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String identification;
    private String concreteGrade;
    private String authorityOfApproval;

    private Double cement;
    private Double ca1;
    private Double ca2;
    private Double fa;
    private Double water;
    private BigDecimal admixtureKg;
    private BigDecimal admixturePercentage;

    @Column(name = "ac_ratio")
    private Double acRatio;

    @Column(name = "wc_ratio")
    private Double wcRatio;

    private String vendorCode;
    private String plantId;
    private Integer createdBy;
    private LocalDateTime createdDate;

    private Integer updatedBy;
    private LocalDateTime updatedDate;
}