package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinalCallInspectionHeaderRequest {

    private String rlyPoNo;
    private String poDate;
    private String vendorName;

    private String callNo;
    private Integer poQty;
    private String maNo;
    private String maDate;

    private BigDecimal qtyOfferedNow;
    private BigDecimal acceptedQty;
    private BigDecimal rejectedQty;

    private Integer etSleepers;
    private String callDate;
    private Integer noOfBatches;

    private String shift;
    private String plantId;
    private String vendorCode;

    private String createdBy;
    private String updatedBy;
}
