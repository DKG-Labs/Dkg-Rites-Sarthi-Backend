package com.sarthi.Sms.dto.iso;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SmsChemicalAnalysisDto {
    private String heatNumber;
    private String sequenceNumber;
    private BigDecimal nitrogen;
    private BigDecimal oxygen;
    private BigDecimal hydris;
    private BigDecimal degassingVacuum;
    private BigDecimal degassingDuration;
    private String heatRemark;
}
