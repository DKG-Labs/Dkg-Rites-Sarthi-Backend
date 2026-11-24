package com.sarthi.Sms.dto.iso;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeldHardnessDtlDto {
    private String date;
    private String railSection;
    private BigDecimal parentHardnessL;
    private BigDecimal parentHardnessR;
    private BigDecimal hazHardnessL;
    private BigDecimal hazHardnessR;
    private BigDecimal deviationL;
    private BigDecimal deviationR;
    private String remark;
}
