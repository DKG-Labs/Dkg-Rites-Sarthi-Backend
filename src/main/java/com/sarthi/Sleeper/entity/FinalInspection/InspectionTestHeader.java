package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_test_header")
@Data
public class InspectionTestHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long batchId;

    private String sleeperType;

    private String shift;

    private LocalDate testDate;

    private Long createdBy;

    private LocalDateTime createdDate;

    private String status;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private InspectionModule module;

}