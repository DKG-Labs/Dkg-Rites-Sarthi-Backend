package com.sarthi.Sleeper.entity.BatchWeighment;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "manual_weighment")
@Data
public class ManualWeighment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "ca1_actual")
    private Double ca1Actual;

    @Column(name = "ca2_actual")
    private Double ca2Actual;

    @Column(name = "fa_actual")
    private Double faActual;

    @Column(name = "cement_actual")
    private Double cementActual;

    @Column(name = "water_actual")
    private Double waterActual;

    @Column(name = "admixture_actual")
    private Double admixtureActual;

    @Column(name = "source")
    private String source; // MANUAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_weighment_id")
    private BatchWeighment batchWeighment;
}
