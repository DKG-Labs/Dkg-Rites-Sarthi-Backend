package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity for Final Inspection Lot Results
 * Stores detailed inspection results for each lot including test statuses,
 * packing details, hologram information, and remarks
 */
@Entity
@Table(name = "final_inspection_lot_results", indexes = {
    @Index(name = "idx_final_lot_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_final_lot_lot_no", columnList = "lot_no"),
    @Index(name = "idx_final_lot_heat_no", columnList = "heat_no")
})
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class FinalInspectionLotResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String inspectionCallNo;

    @Column(nullable = false)
    private String lotNo;

    private String heatNo;

    // ---- TEST RESULTS (Status: OK, NOT OK, PENDING) ----
    private String calibrationStatus;
    private String visualDimStatus;
    private String hardnessStatus;
    private String inclusionStatus;
    private String deflectionStatus;
    private String toeLoadStatus;
    private String weightStatus;
    private String chemicalStatus;

    // ---- PACKING DETAILS ----
    private Integer ercUsedForTesting;
    private Integer totalRejectedQty;
    private Integer stdPackingNo;
    private Integer bagsWithStdPacking;
    private Integer nonStdBagsCount;

    @Column(columnDefinition = "LONGTEXT")
    private String nonStdBagsQty;

    // ---- HOLOGRAM DETAILS (JSON format) ----
    @Column(columnDefinition = "LONGTEXT")
    private String hologramDetails;

    // ---- REMARKS ----
    @Column(columnDefinition = "TEXT")
    private String remarks;

    // ---- OVERALL LOT STATUS ----
    private String lotStatus;

    // ---- AUDIT FIELDS ----
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

