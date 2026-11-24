package com.sarthi.Sms.dto.iso;

import java.util.List;

import lombok.Data;

@Data
public class RollingVerificationDto {
    private List<TempObsvDto> tempDtlList;
    private List<BloomYardDto> bloomYardDtlList;
    private List<VerificationChargingDto> verificationChargingDtlList;

}
