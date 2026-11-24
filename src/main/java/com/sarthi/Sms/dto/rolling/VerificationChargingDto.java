package com.sarthi.Sms.dto.rolling;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class VerificationChargingDto {
    private String dutyId;
    private String heatNo;
    private BigDecimal length;
    private String defect;
    private Boolean bloomTrackingVerification;
    private Boolean hotBloomsDescaling;
    private String remarks;
}
