package com.sarthi.Sms.dto.qct;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class QctCenterLineDtlDto {
    private Integer id;
    private Integer hardnessReadingCount;
    private BigDecimal meanRsh;
    private BigDecimal maxVariation;

}
