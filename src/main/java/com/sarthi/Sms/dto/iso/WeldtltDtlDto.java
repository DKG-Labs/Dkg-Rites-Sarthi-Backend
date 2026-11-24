package com.sarthi.Sms.dto.iso;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WeldtltDtlDto {
    private String railSection;
    private String machineNumber;
    private LocalDate date;
    private BigDecimal loadApplied;
    private BigDecimal deflection;
    private BigDecimal span;
    private String isBroken;
    private String remark;
}
