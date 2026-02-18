package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "mould_preparation")
@Data
public class MouldPreparation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_shed_no", length = 50)
    private String lineShedNo;

    @Column(name = "preparation_date")
    private LocalDate preparationDate;

    @Column(name = "preparation_time")
    private LocalTime preparationTime;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "bench_no", length = 50)
    private String benchNo;

    @Column(name = "mould_cleaned")
    private Boolean mouldCleaned;   // true / false / null

    @Column(name = "oil_applied")
    private Boolean oilApplied;     // true / false / null

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_by", length = 100)
    private int createdBy;

    @Column(name = "updated_by", length = 100)
    private int updatedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "status")
    private String status;



}
