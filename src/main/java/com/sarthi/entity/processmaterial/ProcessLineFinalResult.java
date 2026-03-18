package com.sarthi.entity.processmaterial;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for storing final inspection results per production line in Process Material Inspection.
 * Stores aggregated results from all process stages.
 */
@Entity
@Table(name = "process_line_final_result", 
    indexes = {
        @Index(name = "idx_proc_final_call_no", columnList = "inspection_call_no"),
        @Index(name = "idx_proc_final_po_no", columnList = "po_no"),
        @Index(name = "idx_proc_final_line_no", columnList = "line_no")
    }
)
@Data
public class ProcessLineFinalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_call_no", nullable = false, length = 50)
    private String inspectionCallNo;

    @Column(name = "po_no", nullable = false, length = 50)
    private String poNo;

    @Column(name = "line_no", nullable = false, length = 20)
    private String lineNo;

    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    @Column(name = "heat_number", length = 50)
    private String heatNumber;

    @Column(name = "date_of_inspection")
    private LocalDate dateOfInspection;

    // Quantities
    @Column(name = "offered_qty")
    private Integer offeredQty;

    @Column(name = "total_manufactured")
    private Integer totalManufactured;

    @Column(name = "total_accepted")
    private Integer totalAccepted;

    @Column(name = "total_rejected")
    private Integer totalRejected;

    // Stage-wise quantities (Manufactured, Accepted, Rejected)
    @Column(name = "shearing_manufactured")
    private Integer shearingManufactured;

    @Column(name = "shearing_accepted")
    private Integer shearingAccepted;

    @Column(name = "shearing_rejected")
    private Integer shearingRejected;

    @Column(name = "turning_manufactured")
    private Integer turningManufactured;

    @Column(name = "turning_accepted")
    private Integer turningAccepted;

    @Column(name = "turning_rejected")
    private Integer turningRejected;

    @Column(name = "mpi_manufactured")
    private Integer mpiManufactured;

    @Column(name = "mpi_accepted")
    private Integer mpiAccepted;

    @Column(name = "mpi_rejected")
    private Integer mpiRejected;

    @Column(name = "forging_manufactured")
    private Integer forgingManufactured;

    @Column(name = "forging_accepted")
    private Integer forgingAccepted;

    @Column(name = "forging_rejected")
    private Integer forgingRejected;

    @Column(name = "quenching_manufactured")
    private Integer quenchingManufactured;

    @Column(name = "quenching_accepted")
    private Integer quenchingAccepted;

    @Column(name = "quenching_rejected")
    private Integer quenchingRejected;

    @Column(name = "tempering_manufactured")
    private Integer temperingManufactured;

    @Column(name = "tempering_accepted")
    private Integer temperingAccepted;

    @Column(name = "tempering_rejected")
    private Integer temperingRejected;

    @Column(name = "visual_check_manufactured")
    private Integer visualCheckManufactured;

    @Column(name = "visual_check_accepted")
    private Integer visualCheckAccepted;

    @Column(name = "visual_check_rejected")
    private Integer visualCheckRejected;

    @Column(name = "dimensions_check_manufactured")
    private Integer dimensionsCheckManufactured;

    @Column(name = "dimensions_check_accepted")
    private Integer dimensionsCheckAccepted;

    @Column(name = "dimensions_check_rejected")
    private Integer dimensionsCheckRejected;

    @Column(name = "hardness_check_manufactured")
    private Integer hardnessCheckManufactured;

    @Column(name = "hardness_check_accepted")
    private Integer hardnessCheckAccepted;

    @Column(name = "hardness_check_rejected")
    private Integer hardnessCheckRejected;

    @Column(name = "testing_finishing_manufactured")
    private Integer testingFinishingManufactured;

    @Column(name = "testing_finishing_accepted")
    private Integer testingFinishingAccepted;

    @Column(name = "testing_finishing_rejected")
    private Integer testingFinishingRejected;


    // Submodule Statuses
    @Column(name = "calibration_status", length = 20)
    private String calibrationStatus;

    @Column(name = "static_check_status", length = 20)
    private String staticCheckStatus;

    @Column(name = "shearing_status", length = 20)
    private String shearingStatus;

    @Column(name = "turning_status", length = 20)
    private String turningStatus;

    @Column(name = "mpi_status", length = 20)
    private String mpiStatus;

    @Column(name = "forging_status", length = 20)
    private String forgingStatus;

    @Column(name = "quenching_status", length = 20)
    private String quenchingStatus;

    @Column(name = "tempering_status", length = 20)
    private String temperingStatus;

    @Column(name = "final_check_status", length = 20)
    private String finalCheckStatus;

    @Column(name = "testing_finishing_status", length = 20)
    private String testingFinishingStatus;

    // Final Status
    @Column(name = "status", length = 30)
    private String status; // ACCEPTED / REJECTED / PENDING

    @Column(name = "overall_status", length = 30)
    private String overallStatus; // ACCEPTED / PARTIALLY_ACCEPTED / REJECTED / PENDING

    @Column(name = "remarks", length = 1000)
    private String remarks;

    // Audit fields
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    private String shift;

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

