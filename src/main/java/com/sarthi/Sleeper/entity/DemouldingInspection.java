package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "demoulding_inspection")
@Data
public class DemouldingInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lineShedNo;

    private LocalDate inspectionDate;

    private String inspectionTime;

    private LocalDate castingDate;

    private String batchNo;

    private String benchNo;

    private String sleeperType;

    private String processStatus;

    private String visualCheck;

    private String dimCheck;

    private String overallRemarks;

    private String createdBy;

    private String updatedBy;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private String status;


    /* One Inspection → Many Defects */

    @OneToMany(
            mappedBy = "inspection",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DemouldingDefectiveSleeper> defectiveSleepers;
}
