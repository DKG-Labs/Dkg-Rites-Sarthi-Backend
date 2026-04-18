package com.sarthi.Sleeper.entity.Aggregate;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "aggregate_flakiness_test")
public class AggregateFlakinessTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_of_testing")
    private String typeOfTesting;
    
    @Column(name="request_id")
    private Long requestId;

    private LocalDate testDate;
    private String consignmentNo;

    // Results Sub-Form 1 (20mm)
    private Double combinedIndex20mm;
    private String result20mm;

    // Results Sub-Form 2 (10mm)
    private Double combinedIndex10mm;
    private String result10mm;

    @OneToMany(mappedBy = "flakinessTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AggregateFlakinessRow> observations;

    // Session Context
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;

    // Audit Fields
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
}
