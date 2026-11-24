package com.sarthi.Sms.dto.iso;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeldingMacroDtlDto {
    private String date;
    // private int month;
    // private int year;
    private String railSection;
    private String machineNumber;
    private BigDecimal headL;
    private BigDecimal headR;
    private BigDecimal webL;
    private BigDecimal webR;
    private BigDecimal footL;
    private BigDecimal footR;
    private String remark;
}
