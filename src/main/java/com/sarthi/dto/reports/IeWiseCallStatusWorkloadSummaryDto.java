package com.sarthi.dto.reports;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IeWiseCallStatusWorkloadSummaryDto {

    private String ieId;

    private String ieName;

    private Long noOfCallsPending;

    private Long noOfCallsUnderInspection;

    private Long noOfCallsPendingForIc;

    private Long noOfCallsOverdue;
}
