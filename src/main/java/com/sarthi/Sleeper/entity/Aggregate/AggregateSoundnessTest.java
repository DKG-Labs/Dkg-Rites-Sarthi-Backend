package com.sarthi.Sleeper.entity.Aggregate;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aggregate_soundness_test")
public class AggregateSoundnessTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="request_id")
    private Long requestId;

    private LocalDate testDate;
    private String consignmentNo;
    private String method;
    private Integer cycles;
    private Double initialWt;
    private Double finalWt;
    private Double lossWt;
    private Double lossPercent;
    private String result;

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
