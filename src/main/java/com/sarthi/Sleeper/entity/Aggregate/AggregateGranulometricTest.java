package com.sarthi.Sleeper.entity.Aggregate;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "aggregate_granulometric_test")
public class AggregateGranulometricTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_of_testing")
    private String typeOfTesting;
    
    @Column(name="request_id")
    private Long requestId;

    @Column(name = "test_date")
    private LocalDate testDate;
    
    @Column(name = "consignment_no")
    private String consignmentNo;

    // Mix Proportions
    private Double mixCa1;
    private Double mixCa2;
    private Double mixFa;

    // Sample Weights
    private Double wtCa1;
    private Double wtCa2;
    private Double wtFa;

    @OneToMany(mappedBy = "granulometricTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AggregateGranulometricRow> observations;

    // Session Context
    private String shift;
    private String lineNo;
    
    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    // Audit Fields
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
}
