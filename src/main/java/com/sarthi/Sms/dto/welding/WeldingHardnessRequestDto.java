package com.sarthi.Sms.dto.welding;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WeldingHardnessRequestDto {

    private String machineNo;
    private BigDecimal parentHardnessL;
    private BigDecimal parentHardnessR;
    private BigDecimal hazHardnessL;
    private BigDecimal hazHardnessR;
    private BigDecimal deviationL;
    private BigDecimal deviationR;
    private String remark;
    private String dutyId;
    private String testType;
    private String jointNo;
}
