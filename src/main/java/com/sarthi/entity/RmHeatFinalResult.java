package com.sarthi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing per-heat final results for Raw Material Inspection.
 * Stores final inspection results, submodule statuses, and cumulative summary data.
 */
@Entity
@Table(
    name = "rm_heat_final_result",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_call_heat",
            columnNames = {"inspection_call_no", "heat_no"}
        )
    },
    indexes = {
        @Index(name = "idx_rm_heat_call_no", columnList = "inspection_call_no"),
        @Index(name = "idx_rm_heat_heat_no", columnList = "heat_no"),
        @Index(name = "idx_rm_heat_status", columnList = "status"),
        @Index(name = "idx_rm_heat_overall_status", columnList = "overall_status")
    }
)
@Data
public class RmHeatFinalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------
    // Identification
    // -------------------------
    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "heat_no", nullable = false, length = 50)
    private String heatNo;

    // -------------------------
    // Weights (MT)
    // -------------------------
    @Column(name = "weight_offered_mt", precision = 12, scale = 4)
    private BigDecimal weightOfferedMt;

    @Column(name = "weight_accepted_mt", precision = 12, scale = 4)
    private BigDecimal weightAcceptedMt;

    @Column(name = "weight_rejected_mt", precision = 12, scale = 4)
    private BigDecimal weightRejectedMt;

    @Column(name = "accepted_qty_mt", precision = 12, scale = 4)
    private BigDecimal acceptedQtyMt;

    // -------------------------
    // Submodule Statuses
    // -------------------------
    @Column(name = "calibration_status", length = 20)
    private String calibrationStatus;

    @Column(name = "visual_status", length = 20)
    private String visualStatus;

    @Column(name = "dimensional_status", length = 20)
    private String dimensionalStatus;

    @Column(name = "material_test_status", length = 20)
    private String materialTestStatus;

    @Column(name = "packing_status", length = 20)
    private String packingStatus;

    // -------------------------
    // Final Status
    // -------------------------
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(
        name = "overall_status",
        length = 20
    )
    private String overallStatus; // ACCEPTED / PARTIALLY_ACCEPTED / REJECTED / PENDING

    // -------------------------
    // Cumulative Summary (per Inspection Call)
    // -------------------------
    @Column(name = "total_heats_offered")
    private Integer totalHeatsOffered;

    @Column(name = "total_qty_offered_mt", precision = 12, scale = 4)
    private BigDecimal totalQtyOfferedMt;

    @Column(name = "no_of_bundles")
    private Integer noOfBundles;

    @Column(name = "no_of_erc_finished")
    private Integer noOfErcFinished;

    @Column(name = "offered_earlier")
    private Integer offeredEarlier;

    // -------------------------
    // Remarks
    // -------------------------
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "shift_of_inspection", length = 20)
    private String shiftOfInspection;

    @Column(name = "sealing_type", length = 50)
    private String sealingType;

    @Column(name = "steel_stamp_number", length = 255)
    private String steelStampNumber;

    @Column(name = "hologram_details", columnDefinition = "TEXT")
    private String hologramDetails;

    @Column(name = "date_of_inspection")
    private LocalDateTime dateOfInspection;

    // -------------------------
    // Audit Fields
    // -------------------------
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}

