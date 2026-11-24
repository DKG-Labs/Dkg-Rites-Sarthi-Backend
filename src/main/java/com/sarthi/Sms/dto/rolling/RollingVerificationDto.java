package com.sarthi.Sms.dto.rolling;

import java.util.List;

import lombok.Data;

@Data
public class RollingVerificationDto {
    private String dutyId;
    private String remarks;
    List<TempObservationDto> tempObservationDtls;
    List<VerificationBloomYardDto> bloomYardDtls;
    List<VerificationChargingDto> chargingTableDtls;
}
