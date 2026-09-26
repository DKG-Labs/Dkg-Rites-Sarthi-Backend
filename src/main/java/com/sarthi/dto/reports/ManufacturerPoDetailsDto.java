package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturerPoDetailsDto {
    private String poNumber;
    private String poDate;
    private Long poQuantity;
    private Long totalFinalInspected;
    private Integer openInspectionCalls;
    private Long poBalanceQuantity;
}
