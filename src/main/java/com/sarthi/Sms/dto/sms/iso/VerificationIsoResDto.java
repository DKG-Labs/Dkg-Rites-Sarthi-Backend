package com.sarthi.Sms.dto.sms.iso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationIsoResDto {
    private String heatNumber;
    private String heatStage;
    private Integer turnDownTemp;
    private String turnDownTempWv;
    private BigDecimal degassingVacuum;
    private String degassingVacuumWv;
    private Integer degassingDuration;
    private String degassingDurationWv;
    private Integer castingTemp;
    private Integer castingTemp2;
    private Integer numberOfCoBlooms;
    private Boolean isLadleToTundishUsed;
    private Boolean isTundishToMouldUsed;
}
