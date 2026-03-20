package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sgci_insert_audit")
@Data
public class SgciInsertAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "consignment_no")
    private String consignmentNo;

    @Column(name = "lot_no")
    private String lotNo;

    private String supplier;

    private String type;

    @Column(name = "rites_ic")
    private String ritesIc;

    // Summary fields
    private Integer checked;
    private Integer accepted;
    private Integer rejected;
    
    @Column(name = "rejection_pct")
    private Double rejectionPct;

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

    @OneToMany(mappedBy = "audit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SgciInsertReading> readings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
