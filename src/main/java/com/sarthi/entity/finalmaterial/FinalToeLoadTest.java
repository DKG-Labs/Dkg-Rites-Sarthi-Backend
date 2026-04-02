package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * Parent Entity for Final Inspection - Toe Load Test
 *
 * Stores ONE row per inspection session for a given Lot + Heat + Inspection Call.
 * Holds only header-level and overall-result data.
 * Supports pausing and resuming an inspection.
 *
 * Design Semantics:
 * - One row in final_toe_load_test = one inspection session.
 * - Many rows in final_toe_load_test_sample = all sample readings.
 * - Overall status is stored ONLY in this table.
 * - created_by is set ONLY on first insert and never changed.
 * - updated_by is set on EVERY subsequent update.
 */
@Entity
@Table(name = "final_toe_load_test", indexes = {
    @Index(name = "idx_final_toe_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_final_toe_lot_no", columnList = "lot_no"),
    @Index(name = "idx_final_toe_heat_no", columnList = "heat_no"),
    @Index(name = "idx_final_toe_unique", columnList = "inspection_call_no,lot_no,heat_no", unique = true)
})
@Data
public class FinalToeLoadTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * External workflow call number for this inspection.
     */
    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    /**
     * Lot number under inspection.
     */
    @Column(name = "lot_no", nullable = false, length = 100)
    private String lotNo;

    /**
     * Heat number under inspection.
     */
    @Column(name = "heat_no", nullable = false, length = 100)
    private String heatNo;

    /**
     * Quantity offered for this lot + heat.
     */
    @Column(name = "qty_no")
    private Integer qtyNo;

    /**
     * Overall final result of the inspection.
     * Allowed values: OK, NOT_OK, PENDING.
     * This is the ONLY status column in the entire design.
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    /**
     * Total number of rejected samples across ALL samplings.
     * Can be derived from the child table or stored for fast UI display.
     */
    @Column(name = "rejected")
    private Integer rejected;

    /**
     * Final inspector remarks.
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // ========== Audit Fields ==========

    /**
     * User who FIRST creates the inspection record.
     * This must be set ONLY on the very first insert.
     * Must NOT be changed on subsequent updates.
     */
    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    /**
     * Timestamp when the inspection record was first created.
     * Must NOT be changed on subsequent updates.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * User who last updated the inspection record.
     * This must be set on EVERY subsequent save (when inspection is paused/resumed or more samples are added).
     */
    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    /**
     * Timestamp when the inspection record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ========== Relationships ==========

    /**
     * Child samples for this inspection session.
     * One-to-many relationship with FinalToeLoadTestSample.
     */
    @OneToMany(mappedBy = "finalToeLoadTest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FinalToeLoadTestSample> samples;

    // ========== Lifecycle Callbacks ==========

    /**
     * Called on FIRST insert.
     * Sets created_by, created_at, updated_by, updated_at, and status = PENDING.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    /**
     * Called on EVERY update.
     * Updates updated_at and updated_by.
     * created_by and created_at are protected by updatable = false.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

