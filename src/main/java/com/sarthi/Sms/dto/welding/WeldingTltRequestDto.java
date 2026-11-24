package com.sarthi.Sms.dto.welding;



import java.math.BigDecimal;

import lombok.Data;

@Data
public class WeldingTltRequestDto {

    private String machineNo;
    private BigDecimal span;
    private BigDecimal loadApplied;
    private BigDecimal deflection;
    private BigDecimal loadAt20MmDeflection;
    private Boolean isBroken;
    private String remark;
    private String testType;
    private String jointNo;
    private String dutyId;
}

