package com.sarthi.Sleeper.entity.FinalInspection;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_test_result")
@Data
public class InspectionTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sleeperId;

    private String sleeperNo;

    private String result;

    private String rejectionReason;
    private Long moduleId;

    private Boolean active;     // true = current, false = old
    private LocalDateTime updatedDate;
    private Long updatedBy;

    @ManyToOne
    @JoinColumn(name = "test_header_id")
    private InspectionTestHeader testHeader;

}
