package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RailPadPoLifeCycle3rdLevelDto {
    private Integer slNo;
    private String callNo;
    private Double offeredQty;
    private Double acceptedQty;
    private Double rejectedQty;
    private Double rejectionPercentage;
}
