package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * Parent Entity for Final Inspection - Dimensional Inspection
 *
 * Stores ONE row per inspection session for a given (inspectionCallNo, lotNo, heatNo).
 * Holds only header-level and overall-result data.
 * Supports pausing and resuming an inspection.
 *
 * Design Semantics:
 * - One row in final_dimensional_inspection = one inspection session.
 * - Many rows in final_dimensional_inspection_samples = all sample readings.
 * - Overall status is stored ONLY in this table.
 * - created_by is set ONLY on first insert and never changed.
 * - updated_by is set on EVERY subsequent update.
 */
@Entity
@Table(name = "final_dimensional_inspection", indexes = {
    @Index(name = "idx_final_dim_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_final_dim_lot_no", columnList = "lot_no"),
    @Index(name = "idx_final_dim_heat_no", columnList = "heat_no"),
    @Index(name = "idx_final_dim_status", columnList = "status")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uk_final_dim_unique", columnNames = {"inspection_call_no", "lot_no", "heat_no"})
})
@Data
public class FinalDimensionalInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    @Column(name = "lot_no", nullable = false, length = 100)
    private String lotNo;

    @Column(name = "heat_no", nullable = false, length = 100)
    private String heatNo;

    @Column(name = "sample_size", nullable = false)
    private Integer sampleSize;

    /**
     * Overall final result of the inspection.
     * Allowed values: OK, NOT_OK, PENDING.
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    /**
     * Total number of rejected samples across ALL samplings.
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
     * User ID who created this inspection record.
     * This is set ONLY on first insert and never changed.
     */
    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    /**
     * Timestamp when the inspection record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * User ID who last updated this inspection record.
     * This is set on EVERY subsequent update.
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Timestamp when the inspection record was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== Relationships ==========

    /**
     * Child samples for this inspection.
     * One inspection can have multiple sampling rounds (1st sampling, 2nd sampling, etc.)
     * EAGER loading ensures samples are loaded with parent for API responses.
     */
    @OneToMany(mappedBy = "finalDimensionalInspection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FinalDimensionalInspectionSample> samples;

    // ========== Lifecycle Callbacks ==========

    /**
     * Called on FIRST insert.
     * Sets created_at, updated_at, and status = PENDING if not set.
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
     * Updates updated_at.
     * created_by and created_at are protected by updatable = false.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

