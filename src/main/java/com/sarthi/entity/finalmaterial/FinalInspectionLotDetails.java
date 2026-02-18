package com.sarthi.entity.finalmaterial;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for Final Inspection Lot Details
 * Stores individual lot information for Final inspection (one-to-many relationship)
 */
@Entity
@Table(name = "final_inspection_lot_details")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class FinalInspectionLotDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ---- RELATION TO FINAL INSPECTION DETAILS ----
    @Column(name = "final_detail_id", nullable = false)
    private Long finalDetailId;

    // ---- LOT INFORMATION ----
    private String lotNumber;

    // ---- HEAT INFORMATION ----
    private String heatNumber;
    private String manufacturer;
    private String manufacturerHeat;

    // ---- QUANTITY INFORMATION ----
    private Integer offeredQty;
    private Integer qtyAccepted;
    private Integer qtyRejected;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    // ---- ADDITIONAL TRACKING ----
    private Long processIcId;
    private String processIcNumber;

    @Column(name = "no_of_bags")
    private Integer noOfBags; // Number of bags offered

    // ---- TIMESTAMPS ----
    private LocalDateTime createdAt;
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

