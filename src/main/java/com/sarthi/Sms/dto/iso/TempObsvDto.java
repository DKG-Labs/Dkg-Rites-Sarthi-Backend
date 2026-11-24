package com.sarthi.Sms.dto.iso;

import java.sql.Time;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TempObsvDto {
    private Time createdAt;
    private Integer finishingTemperature;
    private Integer soakingZoneTemperature;
}
