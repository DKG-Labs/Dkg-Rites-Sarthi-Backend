package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "water_cube_strength_detail")
@Data
public class WaterCubeStrengthDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_number")
    private Integer sampleNumber;

    @Column(name = "cube_index")
    private Integer cubeIndex;

    @Column(name = "cube_id")
    private String cubeId;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "load_kn")
    private Double loadKn;

    @Column(name = "strength_nmm2")
    private Double strengthNmm2;

    @Column(name = "testing_date")
    private LocalDate testingDate;

    @Column(name = "testing_time")
    private LocalTime testingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strength_test_id")
    private WaterCubeStrengthTest strengthTest;
}
