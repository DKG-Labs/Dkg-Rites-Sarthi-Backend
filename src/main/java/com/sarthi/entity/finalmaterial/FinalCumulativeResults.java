package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Entity for Final Inspection Cumulative Results
 * Stores cumulative inspection data including quantities offered, passed, rejected
 */
@Entity
@Table(name = "final_cumulative_results", indexes = {
    @Index(name = "idx_final_cumul_call_no", columnList = "inspection_call_no"),
    @Index(name = "idx_final_cumul_po_no", columnList = "po_no")
})
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class FinalCumulativeResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String inspectionCallNo;

    @Column(nullable = false)
    private String poNo;

    // ---- CUMULATIVE QUANTITIES ----
    private Integer poQty;
    private Integer cummQtyOfferedPreviously = 0;
    private Integer cummQtyPassedPreviously = 0;
    private Integer qtyNowOffered;
    private Integer qtyNowPassed;
    private Integer qtyNowRejected;
    private Integer qtyStillDue;

    // ---- SAMPLING DETAILS ----
    private Integer totalSampleSize;
    private Integer bagsForSampling;
    private Integer bagsOffered;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

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

