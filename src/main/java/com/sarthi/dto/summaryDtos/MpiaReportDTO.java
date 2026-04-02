package com.sarthi.dto.summaryDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpiaReportDTO {
    private String manufacture;
    private Double totalInspected;
    private Double totalAccepted;
    private Double totalRejected;
    private Double rejectionPercent;
}
