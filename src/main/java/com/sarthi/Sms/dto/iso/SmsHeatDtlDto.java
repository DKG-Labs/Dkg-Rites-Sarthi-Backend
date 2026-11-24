package com.sarthi.Sms.dto.iso;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsHeatDtlDto {
    private String heatNumber;
    private String heatStage;
    private String heatRemark;
    private Integer turnDownTemp;
    private BigDecimal degassingVacuum;
    private BigDecimal degassingDuration;
    private Integer castingTemp;
    private String casterNumber;
    private String sequenceNumber;
    private BigDecimal hydris;
    private Boolean isProbeDipped;
    private Boolean isHydrogenBw80And100;
    private BigDecimal nitrogen;
    private BigDecimal oxygen;
    private Integer numberOfPrimeBlooms;
    private BigDecimal primeBloomsLength;
    private BigDecimal primeBloomsTotalLength;
    private Integer numberOfCoBlooms;
    private BigDecimal coBloomsLength;
    private BigDecimal coBloomsTotalLength;
    private Integer numberOfRejectedBlooms;
    private BigDecimal rejectedBloomsLength;
    private BigDecimal rejectedBloomsTotalLength;
    private BigDecimal weightOfPrimeBlooms;
    private BigDecimal weightOfCoBlooms;
    private BigDecimal weightOfRejectedBlooms;
    private BigDecimal totalCastWt;
    private Boolean isDiverted;
    private String sentToLadle;
    
    // Bloom Details
    private String castNumber;
    private String bloomIdentification;
    private BigDecimal lengthOfBlooms;
    private String surfaceConditionOfBlooms;
    private Integer numberOfPrimeBloomsRejected;
    private Integer numberOfCoBloomsRejected;
    private String remark;

    private String turnDownTempWv;
    private String degassingVacuumWv;
    private String degassingDurationWv;

    private Boolean isLadleToTundishUsed;
    private Boolean isTundishToMouldUsed;
}
