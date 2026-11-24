package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FinalCallInspectionHeaderResponse {

    private Long id;

    private String rlyPoNo;
    private LocalDate poDate;
    private String vendorName;

    private String callNo;
    private BigDecimal poQty;
    private String maNo;
    private LocalDate maDate;

    private BigDecimal qtyOfferedNow;
    private BigDecimal acceptedQty;
    private BigDecimal rejectedQty;

    private Integer etSleepers;
    private LocalDate callDate;
    private Integer noOfBatches;

    private String shift;
    private String plantId;
    private String vendorCode;
}
