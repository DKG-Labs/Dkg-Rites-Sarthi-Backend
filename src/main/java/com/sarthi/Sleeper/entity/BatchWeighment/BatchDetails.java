package com.sarthi.Sleeper.entity.BatchWeighment;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "batch_details")
@Data
public class BatchDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "proportion_status")
    private String proportionStatus; // OK / NOT_OK


    // ===== Reference Values =====

    @Column(name = "ca1_ref")
    private Double ca1Ref;

    @Column(name = "ca2_ref")
    private Double ca2Ref;

    @Column(name = "fa_ref")
    private Double faRef;

    @Column(name = "cement_ref")
    private Double cementRef;

    @Column(name = "water_ref")
    private Double waterRef;

    @Column(name = "admixture_ref")
    private Double admixtureRef;


    // ===== Manual Set Values =====

    @Column(name = "ca1_set")
    private Double ca1Set;

    @Column(name = "ca2_set")
    private Double ca2Set;

    @Column(name = "fa_set")
    private Double faSet;

    @Column(name = "cement_set")
    private Double cementSet;

    @Column(name = "water_set")
    private Double waterSet;

    @Column(name = "admixture_set")
    private Double admixtureSet;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_weighment_id")
    private BatchWeighment batchWeighment;
}
