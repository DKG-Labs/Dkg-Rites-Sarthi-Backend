package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sample_other_bench")
@Data
public class SampleOtherBench {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String benchNo;

    private String sleeperSequence;

    private String cubeCode; // ex: 409C, 56C

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id")
    private SteamCubeSampleDeclaration sample;
}
