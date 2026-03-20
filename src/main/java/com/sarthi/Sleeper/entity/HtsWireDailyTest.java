package com.sarthi.Sleeper.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hts_wire_daily_test")
@Data
public class HtsWireDailyTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "consignment_no")
    private String consignmentNo;

    @Column(name = "coil_no")
    private String coilNo;

    @Column(name = "inventory_id")
    private String inventoryId;

    @Column(name = "nominal_weight")
    private Double nominalWeight;

    @Column(name = "lay_length")
    private Double layLength;

    @Column(name = "strand_diameter")
    private Double strandDiameter;

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
