package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalCallsSummaryDTO {

    private Long totalOpenCalls;
    private Long totalUnderInspectionCalls;
    private Long totalPendingCalls;
}
