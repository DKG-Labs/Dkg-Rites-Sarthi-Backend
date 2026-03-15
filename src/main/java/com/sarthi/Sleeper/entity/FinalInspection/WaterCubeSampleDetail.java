package com.sarthi.Sleeper.entity.FinalInspection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "water_cube_sample_detail")
@Data
public class WaterCubeSampleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sample_number")
    private Integer sampleNumber; // 1 or 2

    @Column(name = "cube_number")
    private Integer cubeNumber; // 1, 2, or 3

    @Column(name = "bench_number")
    private String benchNumber;

    private String sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declaration_id")
    @JsonIgnore
    private WaterCubeSampleDeclaration declaration;
}
