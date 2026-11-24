package com.sarthi.Sms.dto.qct;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class QctResidualDtlDto {
    private Integer id;
    private BigDecimal stress;
    private String stressNature;
}
