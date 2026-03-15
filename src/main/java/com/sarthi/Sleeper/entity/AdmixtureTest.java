package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "admixture_test")
@Data
public class AdmixtureTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "consignment_no")
    private String consignmentNo;

    private Double dosage;

    private Double density;

    private Double ph;

    private String result;

    // Session Context
    private String shift;

    @Column(name = "line_no")
    private String lineNo;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    // Audit Fields
    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
