package com.sarthi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatDetailsDto {
    private String callNo;
    private String heatNo;
    private String tcNo;
    private BigDecimal offeredQty;
    private BigDecimal acceptedQty;
    private BigDecimal rejectedQty;
    private BigDecimal weightAcceptedMt;
    private BigDecimal weightRejectedMt;
}
