package com.sarthi.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RailPadMprReportDto {
    private String rly;
    private String poNo;
    private String manufacturer;
    private Double poQty;
    private String uom;
    private Double dispatchedMonthly;
    private Double totalDispatched;
    private Double balance;
}
