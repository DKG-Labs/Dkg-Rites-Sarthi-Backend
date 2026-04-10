package com.sarthi.Sleeper.entity.SteamCubeT;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "steam_cube_testing_details")
@Data
public class SteamCubeTestingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cubeNo;

    private LocalDate dateOfTesting;
    private LocalTime time;

    private Double ageHours;
    private Double weightKgs;
    private Double loadKn;
    private Double strength;

    // ===== RELATION =====
    @ManyToOne
    @JoinColumn(name = "steam_cube_testing_id")
    private SteamCubeTesting steamCubeTesting;
}