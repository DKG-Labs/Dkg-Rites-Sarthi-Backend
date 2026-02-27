package com.sarthi.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for per-heat final results.
 * Stores final inspection results, submodule statuses, and cumulative summary data.
 */
@Data
public class RmHeatFinalResultDto {

    private String inspectionCallNo;
    private String heatNo;

    // Weights (MT)
    private BigDecimal weightOfferedMt;
    private BigDecimal weightAcceptedMt;
    private BigDecimal weightRejectedMt;
    private BigDecimal acceptedQtyMt;

    private Integer offeredEarlier;

    // Submodule Statuses
    private String calibrationStatus;
    private String visualStatus;
    private String dimensionalStatus;
    private String materialTestStatus;
    private String packingStatus;

    // Final Status
    private String status;
    private String overallStatus; // ACCEPTED / PARTIALLY_ACCEPTED / REJECTED / PENDING

    // Cumulative Summary (per Inspection Call)
    private Integer totalHeatsOffered;
    private BigDecimal totalQtyOfferedMt;
    private Integer noOfBundles;
    private Integer noOfErcFinished;

    // Remarks
    private String remarks;
    
    // Shift
    private String shift;

    // Sealing and Hologram Details
    private String sealingType;
    private String steelStampNumber;
    private String hologramDetails;

    private java.time.LocalDate dateOfInspection;

    // Audit fields
    private String createdBy;
    private String updatedBy;
}

