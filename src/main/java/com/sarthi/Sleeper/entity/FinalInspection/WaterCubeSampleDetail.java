package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "water_cube_sample_detail")
@Data
public class WaterCubeSampleDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer sampleNumber;

    private Integer cubeNumber;

    private String benchNumber;

    private String sequence;

    @ManyToOne
    @JoinColumn(name = "declaration_id")
    private WaterCubeSampleDeclaration declaration;
}
