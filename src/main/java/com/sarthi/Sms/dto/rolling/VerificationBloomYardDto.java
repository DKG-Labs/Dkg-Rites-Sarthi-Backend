package com.sarthi.Sms.dto.rolling;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class VerificationBloomYardDto {
    private String heatNo;

    private String dutyId;

    private Boolean internalTransferVerification;

    private Boolean heatRegisterVerification;

    private BigDecimal length;

    private String defect;

    private String remarks;
}
