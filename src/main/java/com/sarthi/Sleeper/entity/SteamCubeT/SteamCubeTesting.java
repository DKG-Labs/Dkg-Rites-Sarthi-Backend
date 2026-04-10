package com.sarthi.Sleeper.entity.SteamCubeT;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "steam_cube_testing")
@Data
public class SteamCubeTesting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== PRE-FILLED INFO =====
    private String location;
    private LocalDate dateOfCasting;
    private String batchNo;
    private LocalTime lbcTime;
    private String concreteGrade;

    // ===== RESULT =====
    private Double avgStrength;
    private String result; // OK / Not OK

    // ===== AUDIT =====
    private Integer createdBy;
    private LocalDate createdDate;

    // ===== CHILD =====
    @OneToMany(mappedBy = "steamCubeTesting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SteamCubeTestingDetails> cubeDetails;
}
