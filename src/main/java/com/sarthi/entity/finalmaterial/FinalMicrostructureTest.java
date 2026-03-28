package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * Parent Entity for Final Inspection - Microstructure Test
 *
 * Stores ONE row per inspection session for a given Lot + Heat + Inspection Call.
 * Holds only header-level and overall-result data.
 * Supports pausing and resuming an inspection.
 *
 * Design Semantics:
 * - One row in final_microstructure_test = one inspection session.
 * - Many rows in final_microstructure_sample = all sample readings.
 * - Overall status is stored ONLY in this table.
 * - created_by is set ONLY on first insert and never changed.
 * - updated_by is set on EVERY subsequent update.
 */
@Entity
@Table(name = "final_microstructure_test", indexes = {
    @Index(name = "idx_final_micro_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_final_micro_lot_no", columnList = "lot_no"),
    @Index(name = "idx_final_micro_heat_no", columnList = "heat_no")
})
@Data
public class FinalMicrostructureTest {

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
     * Sample size for this inspection.
     */
    @Column(name = "sample_size", nullable = false)
    private Integer sampleSize;

    /**
     * Quantity offered for this lot + heat.
     */
    @Column(name = "qty")
    private Integer qty;

    /**
     * Overall final result of the inspection.
     * Allowed values: OK, NOT_OK, PENDING.
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    /**
     * Total number of rejected samples.
     */
    @Column(name = "rejected")
    private Integer rejected = 0;

    /**
     * Final inspector remarks.
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // ========== Audit Fields ==========

    /**
     * User ID who created this inspection record.
     */
    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    /**
     * Timestamp when the inspection record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * User who last updated the inspection record.
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
     * Child samples for this inspection session.
     */
    @OneToMany(mappedBy = "finalMicrostructureTest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FinalMicrostructureSample> samples;

    // ========== Lifecycle Callbacks ==========

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

