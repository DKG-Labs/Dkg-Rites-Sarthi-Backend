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
public class DiversionItemDto {
    private Long id;
    private String heatNo;
    private String tcNo;
    private String lotNo;
    private BigDecimal acceptedQty;
    private BigDecimal downstreamConsumedQty;
    private BigDecimal availableBalanceQty;
    private BigDecimal diversionQty;
    private BigDecimal remainingBalanceQty;
}
