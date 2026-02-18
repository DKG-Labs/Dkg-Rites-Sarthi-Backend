package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sample_cube")
@Data
public class SampleCube {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String benchNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id")
    private SteamCubeSampleDeclaration sample;
}
