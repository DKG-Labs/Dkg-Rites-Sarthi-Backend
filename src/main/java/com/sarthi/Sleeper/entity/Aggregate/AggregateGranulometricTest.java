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
    
    @Column(name="request_id")
    private Long requestId;

    private LocalDate testDate;
    private String consignmentNo;

    @OneToMany(mappedBy = "granulometricTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AggregateGranulometricRow> observations;

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
