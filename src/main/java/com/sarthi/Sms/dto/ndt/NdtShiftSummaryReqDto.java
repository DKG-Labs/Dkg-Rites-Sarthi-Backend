package com.sarthi.Sms.dto.ndt;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class NdtShiftSummaryReqDto {

    private String dutyId;
    private BigDecimal rollingTableSpeed;
    private Integer totalRailsInspected;
    private Integer totalUtMarkedRails;
    private Integer totalEctMarkedRails;
    private Integer bodyStraightnessResultA;
    private Integer bodyStraightnessResultA1;
    private Integer bodyStraightnessResultMarked;
    private Integer totalProfileMarked;
    private Integer railLength;
    private Integer totalUtOkRails;
    private Integer totalUtSuspectedRails;
    private Integer totalUtRejectedRails;
    private Integer totalRecycledRails;
    private Integer ectSuspectedRails;
    private String remarks;
}
