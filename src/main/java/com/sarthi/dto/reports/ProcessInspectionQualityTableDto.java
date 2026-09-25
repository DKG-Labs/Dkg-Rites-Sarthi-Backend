package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInspectionQualityTableDto {
    private String manufacturerName;
    private Long totalInspected;
    private Long totalAccepted;
    private Long totalRejected;
    private Double rejectionPercent;
    private Double shearingRejectionPercent;
    private Double turningRejectionPercent;
    private Double mpiRejectionPercent;
    private Double forgingRejectionPercent;
    private Double quenchingRejectionPercent;
    private Double temperingRejectionPercent;
}
