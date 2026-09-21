package com.sarthi.Sms.dto.sms.iso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChemicalAnalysisIsoResDto {
    private String heatNumber;
    private String sequenceNumber;
    private BigDecimal nitrogen;
    private BigDecimal oxygen;
    private BigDecimal hydris;
    private BigDecimal degassingVacuum;
    private Integer degassingDuration;
    private String heatRemark;
}
