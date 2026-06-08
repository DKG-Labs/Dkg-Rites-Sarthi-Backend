package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RailPadFinalInspectionSummaryDto {
    private long acceptedQtyNos;
    private long acceptedQtySet;
    private long rejectedQtyNos;
    private long rejectedQtySet;
}
