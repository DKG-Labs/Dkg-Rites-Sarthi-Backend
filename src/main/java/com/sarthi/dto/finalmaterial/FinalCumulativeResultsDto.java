package com.sarthi.dto.finalmaterial;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Final Cumulative Results
 * Stores cumulative inspection data including quantities offered, passed, rejected
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalCumulativeResultsDto {

    private String inspectionCallNo;
    private String poNo;

    // ---- CUMULATIVE QUANTITIES ----
    private Integer poQty;
    private Integer cummQtyOfferedPreviously;
    private Integer cummQtyPassedPreviously;
    private Integer qtyNowOffered;
    private Integer qtyNowPassed;
    private Integer qtyNowRejected;
    private Integer qtyStillDue;

    // ---- SAMPLING DETAILS ----
    private Integer totalSampleSize;
    private Integer bagsForSampling;
    private Integer bagsOffered;

    private java.time.LocalDate dateOfInspection;

    // ---- AUDIT FIELDS ----
    private String createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private String updatedBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime updatedAt;
}

