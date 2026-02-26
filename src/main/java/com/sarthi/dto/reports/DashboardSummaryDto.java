package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private long poIssued;
    private long poQuantityNos;
    private double poQuantityMt;
    private long finalInspectionQuantity;
    private double avgProductionPerDay;
    private double processRejectionPercentage;
    private double finalRejectionPercentage;
    private double rmRejectionPercentage;
}
