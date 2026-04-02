package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Entity for Final Inspection - Visual Inspection
 * Stores visual inspection data for final inspection including rejected piece counts and status
 *
 * Design Semantics:
 * - One row per (inspectionCallNo, lotNo, heatNo) combination (enforced by unique constraint)
 * - Supports UPSERT pattern: INSERT ... ON DUPLICATE KEY UPDATE
 * - created_by is set ONLY on first insert and never changed (immutable)
 * - updated_by is set on EVERY subsequent update
 * - Audit fields track who made changes and when
 */
@Entity
@Table(name = "final_visual_inspection", indexes = {
    @Index(name = "idx_final_visual_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_final_visual_lot_no", columnList = "lot_no"),
    @Index(name = "idx_final_visual_heat_no", columnList = "heat_no"),
    @Index(name = "idx_final_visual_call_lot", columnList = "inspection_call_no, lot_no")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uk_final_visual_unique", columnNames = {"inspection_call_no", "lot_no", "heat_no"})
})
@Data
public class FinalVisualInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    @Column(name = "lot_no", length = 100)
    private String lotNo;

    @Column(name = "heat_no", length = 100)
    private String heatNo;

    @Column(name = "first_sample_rejected")
    private Integer firstSampleRejected = 0;

    @Column(name = "second_sample_rejected")
    private Integer secondSampleRejected = 0;

    @Column(name = "total_rejected")
    private Integer totalRejected = 0;

    @Column(name = "status", length = 20)
    private String status = "PENDING"; // Values: 'OK', 'NOT_OK', 'PENDING'

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
     * This must be set on EVERY subsequent save (when inspection is paused/resumed or data is updated).
     */
    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    /**
     * Timestamp when the inspection record was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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

