package com.sarthi.Sms.dto.vi;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ViAcceptDtlsDto {
    private BigDecimal acceptedLength;
    private Integer acceptedNo;
    private String railClass;
}
