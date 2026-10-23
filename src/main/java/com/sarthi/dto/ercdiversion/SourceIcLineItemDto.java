package com.sarthi.dto.ercdiversion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceIcLineItemDto {
    private String heatNo;
    private String tcNo;
    private String lotNo;
    private BigDecimal acceptedQty;
    private BigDecimal downstreamConsumedQty;
    private BigDecimal previouslyDivertedQty;
    private BigDecimal availableBalanceQty;
    private String unitOfMeasurement;
}
