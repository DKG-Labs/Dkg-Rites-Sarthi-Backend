package com.sarthi.Sleeper.entity.Cement;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "cement_7_day_strength_cube")
@Data
public class Cement7DayStrengthCube {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cast_date")
    private LocalDate castDate;

    @Column(name = "cast_time")
    private LocalTime castTime;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "test_time")
    private LocalTime testTime;

    private Double loadKn;

    private Double strengthNmm2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cement_7_day_strength_id")
    private Cement7DayStrength cement7DayStrength;
}
