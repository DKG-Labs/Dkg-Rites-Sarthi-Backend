package com.sarthi.Sleeper.entity.SteamCuring;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "steam_curing_manual")
@Data
public class SteamCuringManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "chamber")
    private String chamber;

    @Column(name = "min_temp")
    private Double minTemp;

    @Column(name = "max_temp")
    private Double maxTemp;

    @Column(name = "lbc_time")
    private String lbcTime;

    @Column(name = "curing_stage")
    private String curingStage;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "source")
    private String source; // MANUAL


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "steam_curing_id")
    private SteamCuring steamCuring;
}
