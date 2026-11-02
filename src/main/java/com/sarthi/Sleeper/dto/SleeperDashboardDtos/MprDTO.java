package com.sarthi.Sleeper.dto.SleeperDashboardDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MprDTO {

    private int sno;
    private String rly;
    private String poNo;
    private String manufacturer;
    // Unit of measurement of the row: Nos. or Set
    private String uom;

    private Integer poQty;
    private Integer dispatchedInPeriod;
    private Integer totalDispatched;

    private Integer balance;
}