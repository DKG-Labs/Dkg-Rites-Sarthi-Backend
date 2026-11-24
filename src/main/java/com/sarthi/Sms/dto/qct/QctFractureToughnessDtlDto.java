package com.sarthi.Sms.dto.qct;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class QctFractureToughnessDtlDto {
    private Integer id;
    private String parameter;
    private BigDecimal value;
}
