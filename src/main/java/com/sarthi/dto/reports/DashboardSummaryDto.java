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
    private long sleeperPoIssued;
    private long sleeperPoQuantityNos;
    private long sleeperPoQuantitySet;
    private long sleeperIcIssued;
    // Rail Pad specific fields
    private long railPadPoIssued;
    private long railPadPoQuantityNos;
    private long railPadPoQuantitySet;
    private long pendingCalls;
    private long underInspectionCalls;
    private long rejectedInProcess;
    private long rejectedInFinal;
    // Rail Pad avg production / day (pieces produced / active production days, last 30 days)
    private double railPadAvgProductionPerDay;
    // Rail Pad total accepted quantities broken down by UOM
    private long totalAcceptedNos;
    private long totalAcceptedSet;
    // Rail Pad total rejected quantities broken down by UOM
    private long totalRejectedNos;
    private long totalRejectedSet;
    private long totalProcessProduced;
    // Rail Pad rejection percentage (process IE rejections / production declared by vendor)
    private double railPadRejectionPercentage;
}
