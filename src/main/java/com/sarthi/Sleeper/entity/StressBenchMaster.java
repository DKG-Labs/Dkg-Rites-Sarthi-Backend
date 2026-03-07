package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "stress_bench_master")
@Data
public class StressBenchMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer benchNo;

    private Integer benchFrom;

    private Integer benchTo;

    private Integer noOfBenches;

    private String sleeperCategory;

    private Integer mouldsPerBench;

    private String entryType; // RANGE / SINGLE

    private Long createdBy;

    private LocalDateTime createdDate;

    private Long updatedBy;

    private LocalDateTime updatedDate;
}