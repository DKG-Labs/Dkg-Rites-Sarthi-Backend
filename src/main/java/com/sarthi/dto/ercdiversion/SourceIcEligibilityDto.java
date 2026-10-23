package com.sarthi.dto.ercdiversion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceIcEligibilityDto {
    private String certificateNo;
    private String callNo;
    private String poNo;
    private String poSerialNo;
    private LocalDate icDate;
    private BigDecimal totalAcceptedQty;
    private BigDecimal totalConsumedQty;
    private BigDecimal totalDivertedQty;
    private BigDecimal availableBalanceQty;
    private String unitOfMeasurement;
}
