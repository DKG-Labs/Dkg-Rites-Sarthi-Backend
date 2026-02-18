package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Child Entity for Final Inspection - Inclusion Rating Sample
 * 
 * Stores EVERY individual sample value entered in the UI.
 * Supports multiple samplings and dynamic sample counts.
 * 
 * Design Semantics:
 * - One row in final_inclusion_rating = one inspection session.
 * - Many rows in final_inclusion_rating_sample = all sample readings.
 * - sampling_no groups samples into 1st sampling, 2nd sampling, etc.
 * - sample_no preserves the UI order (#1, #2, #3, ...).
 * - Each sample has four rating values: A, B, C, D.
 * - There is NO separate table for sampling rounds.
 * - There is NO per-round status column.
 * - Overall status is stored ONLY in final_inclusion_rating.status.
 */
@Entity
@Table(name = "final_inclusion_rating_sample")
@Data
public class FinalInclusionRatingSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to parent inspection record.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_inclusion_rating_id", nullable = false)
    private FinalInclusionRatingNew finalInclusionRating;

    /**
     * Sampling round number (1st sampling, 2nd sampling, etc.).
     */
    @Column(name = "sampling_no", nullable = false)
    private Integer samplingNo;

    /**
     * Sample number within the sampling round (#1, #2, #3, ...).
     */
    @Column(name = "sample_no", nullable = false)
    private Integer sampleNo;

    /**
     * Inclusion rating value A.
     */
    @Column(name = "sample_value_a", length = 50)
    private String sampleValueA;

    @Column(name = "sample_type_a", length = 50)
    private String sampleTypeA;

    /**
     * Inclusion rating value B.
     */
    @Column(name = "sample_value_b", length = 50)
    private String sampleValueB;

    @Column(name = "sample_type_b", length = 50)
    private String sampleTypeB;

    /**
     * Inclusion rating value C.
     */
    @Column(name = "sample_value_c", length = 50)
    private String sampleValueC;

    @Column(name = "sample_type_c", length = 50)
    private String sampleTypeC;

    /**
     * Inclusion rating value D.
     */
    @Column(name = "sample_value_d", length = 50)
    private String sampleValueD;

    @Column(name = "sample_type_d", length = 50)
    private String sampleTypeD;

    /**
     * Timestamp when this sample value was recorded.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * User who created this sample record.
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    // ========== Lifecycle Callbacks ==========

    /**
     * Called on insert.
     * Sets created_at timestamp.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

