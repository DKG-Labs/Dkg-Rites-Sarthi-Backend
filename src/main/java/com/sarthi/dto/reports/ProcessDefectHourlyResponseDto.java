package com.sarthi.dto.reports;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProcessDefectHourlyResponseDto {

    private Long finalResultId;
    private String callNo;
    private String shift;
    private String lineNo;
    private String lotNumber;
    private String createdBy;
    private String engineer;
    private LocalDate dateOfInspection;
    private LocalDateTime createdAt;

    // Quantities
    private Integer totalManufactured;
    private Integer totalAccepted;
    private Integer totalRejected;

    // Stage-wise totals
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

    // Hourly rows (8 hours)
    private List<ProcessHourlyDefectRowDto> hourlyRows;
}
