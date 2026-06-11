package com.sarthi.dto.reports;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RailPadVendorWiseQualityDto {
    private String manufacture;
    private long totalInspected;
    private long totalAccepted;
    private long totalRejected;
    private String rejectionPercent;
}
