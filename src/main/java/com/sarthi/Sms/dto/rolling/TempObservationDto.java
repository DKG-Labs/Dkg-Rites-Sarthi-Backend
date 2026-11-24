package com.sarthi.Sms.dto.rolling;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TempObservationDto {
    private String dutyId;
    private String createdAt;
    private Integer soakingZoneTemp;
    private Integer finishingTemp;
    private String remarks;
}
