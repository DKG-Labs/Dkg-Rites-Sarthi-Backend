package com.sarthi.dto.processmaterial;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for per-line final results in Process Material Inspection.
 * Stores final inspection results and submodule statuses for each production line.
 */
@Data
public class ProcessLineFinalResultDto {

    private String inspectionCallNo;
    private String poNo;
    private String lineNo;
    private String lotNumber;
    private String heatNumber;
    private String shift;
    private LocalDate dateOfInspection;

    // Quantities
    private Integer offeredQty;
    private Integer totalManufactured;
    private Integer totalAccepted;
    private Integer totalRejected;

    // Stage-wise quantities (Manufactured, Accepted, Rejected)
    private Integer shearingManufactured;
    private Integer shearingAccepted;
    private Integer shearingRejected;

    private Integer turningManufactured;
    private Integer turningAccepted;
    private Integer turningRejected;
    private Integer parallelLengthRejected;
    private Integer fullTurningLengthRejected;
    private Integer turningDiaRejected;

    private Integer mpiManufactured;
    private Integer mpiAccepted;
    private Integer mpiRejected;

    private Integer forgingManufactured;
    private Integer forgingAccepted;
    private Integer forgingRejected;

    private Integer quenchingManufactured;
    private Integer quenchingAccepted;
    private Integer quenchingRejected;

    private Integer temperingManufactured;
    private Integer temperingAccepted;
    private Integer temperingRejected;

    private Integer visualCheckManufactured;
    private Integer visualCheckAccepted;
    private Integer visualCheckRejected;

    private Integer dimensionsCheckManufactured;
    private Integer dimensionsCheckAccepted;
    private Integer dimensionsCheckRejected;

    private Integer hardnessCheckManufactured;
    private Integer hardnessCheckAccepted;
    private Integer hardnessCheckRejected;

    private Integer testingFinishingManufactured;
    private Integer testingFinishingAccepted;
    private Integer testingFinishingRejected;

    // Submodule Statuses
    private String calibrationStatus;
    private String staticCheckStatus;
    private String shearingStatus;
    private String turningStatus;
    private String mpiStatus;
    private String forgingStatus;
    private String quenchingStatus;
    private String temperingStatus;
    private String finalCheckStatus;
    private String testingFinishingStatus;

    // Final Status
    private String status; // ACCEPTED / REJECTED / PENDING
    private String overallStatus; // ACCEPTED / PARTIALLY_ACCEPTED / REJECTED / PENDING
    private String remarks;

    // Audit fields
    private String createdBy;
    private String updatedBy;
}

