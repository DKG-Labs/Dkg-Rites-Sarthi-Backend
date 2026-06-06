package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RailFinalVisualDimensionalInspectionResponseDto {
    private Long id;
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private java.time.LocalDate dateOfShift;

    // Visual Inspection fields
    private Integer visualSamples;
    private Integer visualNotOk;
    private String visualReason;
    private String visualResult;

    // Dimensional Inspection fields
    private Integer dimensionalSamples;
    private Integer dimensionalNotOk;
    private String dimensionalReason;
    private String dimensionalResult;

    // Total Rejected
    private Integer totalRejected;

    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
