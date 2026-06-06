package com.sarthi.dto.reports;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IeOperationalSlaPerformanceSummaryDto {

    private String ieId;

    private String ieName;

    private Long totalCalls;

    private Long overdueCallsAttended;

    private Long callsCancelled;

    private Long callsAccepted;

    private Long callsRejected;

    private Long callsPartiallyAcceptedRejected;

    private Long callsWithheld;

    private Long icIssued;
}
