package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "steam_cube_sample_declaration")
@Data
public class SteamCubeSampleDeclaration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shedNo;

    private String lineNo;

    private LocalDate castingDate;

    private LocalTime lbcTime;

    private String batchNo;

    private String concreteGrade;

    private String chamberNo;

    private LocalDateTime createdAt;

    // Relations

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SampleCube> cubes;

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SampleOtherBench> otherBenches;
}
