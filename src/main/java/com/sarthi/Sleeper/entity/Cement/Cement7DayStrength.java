package com.sarthi.Sleeper.entity.Cement;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cement_7_day_strength")
@Data
public class Cement7DayStrength {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_of_testing")
    private String typeOfTesting;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "consignment_no")
    private String consignmentNo;

    @Column(name = "room_temp")
    private Double roomTemp;

    @Column(name = "normal_consistency")
    private Double normalConsistency;

    @Column(name = "water_required")
    private Double waterRequired;

    @Column(name = "min_strength")
    private Double minStrength;

    @Column(name = "cube_result")
    private String cubeResult;

    private Double soundness;

    @Column(name = "soundness_result")
    private String soundnessResult;

    private String shift;

    @Column(name = "line_no")
    private String lineNo;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "cement7DayStrength",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Cement7DayStrengthCube> cubes = new ArrayList<>();
}
