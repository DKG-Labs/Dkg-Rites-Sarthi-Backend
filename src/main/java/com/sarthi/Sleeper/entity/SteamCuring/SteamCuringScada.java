package com.sarthi.Sleeper.entity.SteamCuring;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "steam_curing_scada")
@Data
public class SteamCuringScada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "batch_no")
    private String batchNo;


    // ===== CA1 =====
    @Column(name = "ca1_set")
    private Double ca1Set;

    @Column(name = "ca1_actual")
    private Double ca1Actual;


    // ===== CA2 =====
    @Column(name = "ca2_set")
    private Double ca2Set;

    @Column(name = "ca2_actual")
    private Double ca2Actual;


    // ===== FA =====
    @Column(name = "fa_set")
    private Double faSet;

    @Column(name = "fa_actual")
    private Double faActual;


    // ===== Cement =====
    @Column(name = "cement_set")
    private Double cementSet;

    @Column(name = "cement_actual")
    private Double cementActual;


    // ===== Water =====
    @Column(name = "water_set")
    private Double waterSet;

    @Column(name = "water_actual")
    private Double waterActual;


    // ===== Admixture =====
    @Column(name = "admixture_set")
    private Double admixtureSet;

    @Column(name = "admixture_actual")
    private Double admixtureActual;


    @Column(name = "total_set")
    private Double totalSet;

    @Column(name = "total_actual")
    private Double totalActual;


    @Column(name = "source")
    private String source; // SCADA


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "steam_curing_id")
    private SteamCuring steamCuring;
}
