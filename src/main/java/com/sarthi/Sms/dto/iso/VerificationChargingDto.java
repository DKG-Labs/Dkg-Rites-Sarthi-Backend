package com.sarthi.Sms.dto.iso;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationChargingDto {
    private String heatNumber;
    private BigDecimal length;
    private String defect;
    private Boolean bloomTrackingVerification;
    private Boolean hotBloomsDescaling;
}
