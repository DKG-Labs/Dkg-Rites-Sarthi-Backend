package com.sarthi.Sleeper.entity.Cement;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cement_specific_surface")
@Data
public class CementSpecificSurface {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "type_of_testing")
    private String typeOfTesting;

    @Column(name = "consignment_no")
    private String consignmentNo;

    @Column(name = "room_temp")
    private Double roomTemp;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "standard_time_ts")
    private Double standardTimeTs;

    @Column(name = "standard_surface_fs")
    private Double standardSurfaceFs;

    @Column(name = "sample_time1")
    private Double sampleTime1;

    @Column(name = "sample_time2")
    private Double sampleTime2;

    @Column(name = "sample_time3")
    private Double sampleTime3;

    @Column(name = "avg_time")
    private Double avgTime;

    @Column(name = "specific_surface_fm")
    private Double specificSurfaceFm;

    @Column(name = "result")
    private String result;

    @Column(name = "shift")
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
}
