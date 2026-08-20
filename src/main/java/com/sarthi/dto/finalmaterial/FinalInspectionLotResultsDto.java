package com.sarthi.dto.finalmaterial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * DTO for Final Inspection Lot Results
 * Stores detailed inspection results for each lot including test statuses,
 * packing details, hologram information, and remarks
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalInspectionLotResultsDto {

    private String inspectionCallNo;
    private String lotNo;
    private String heatNo;

    // ---- TEST RESULTS (Status: OK, NOT OK, PENDING) ----
    private String calibrationStatus;
    private String visualDimStatus;
    private String hardnessStatus;
    private String inclusionStatus;
    private String deflectionStatus;
    private String toeLoadStatus;
    private String weightStatus;
    private String chemicalStatus;

    // ---- PACKING DETAILS ----
    private Integer ercUsedForTesting;
    private Integer totalRejectedQty;
    private Integer stdPackingNo;
    private Integer bagsWithStdPacking;
    private Integer nonStdBagsCount;
    private String nonStdBagsQty;

    // ---- HOLOGRAM DETAILS (JSON format) ----
    private String hologramDetails;

    // ---- REMARKS ----
    private String remarks;

    // ---- OVERALL LOT STATUS ----
    private String lotStatus;

    // ---- AUDIT FIELDS ----
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private String updatedBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;
}

