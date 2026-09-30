package com.sarthi.dto.reports;

import lombok.Data;
import java.util.List;

@Data
public class ProcessDefectSummaryUpdateRequest {

    private Long finalResultId;
    private String updatedBy;

    // Metadata fields editable by user
    private String lineNo;
    private String shift;
    private java.time.LocalDate dateOfInspection;
    private String createdBy; // Employee ID / Code
    private String engineer; // Employee Name (Code)

    // Shift summary quantities
    private Integer totalManufactured;
    private Integer totalAccepted;
    private Integer totalRejected;

    // Stage quantities
    private Integer shearingManufactured;
    private Integer shearingRejected;
    private Integer turningManufactured;
    private Integer turningRejected;
    private Integer mpiManufactured;
    private Integer mpiRejected;
    private Integer forgingManufactured;
    private Integer forgingRejected;
    private Integer quenchingManufactured;
    private Integer quenchingRejected;
    private Integer temperingManufactured;
    private Integer temperingRejected;

    // Granular 8-hour rows
    private List<ProcessHourlyDefectRowDto> hourlyRows;
}
