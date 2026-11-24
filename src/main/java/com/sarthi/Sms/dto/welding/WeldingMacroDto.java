package com.sarthi.Sms.dto.welding;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WeldingMacroDto {
    private String testType;
    private String jointNo;
    private BigDecimal headL;
    private BigDecimal webL;
    private BigDecimal footL;
    private BigDecimal headR;
    private BigDecimal webR;
    private BigDecimal footR;
    private String remarks;
    private String dutyId;
    private String machineNo;
}
