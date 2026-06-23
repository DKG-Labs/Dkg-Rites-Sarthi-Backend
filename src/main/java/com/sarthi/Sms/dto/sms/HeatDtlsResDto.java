package com.sarthi.Sms.dto.sms;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HeatDtlsResDto {
    private String turnDownTempWv;
    private String degassingVacuumWv;
    private String degassingDurationWv;
    private String heatNo;
    private String heatStage;
    private Integer turnDownTemp;
    private BigDecimal degassingVacuum;
    private Integer degassingDuration;
    private Integer castingTemp;
    private Integer castingTemp2;
    private String casterNo;
    private String sequenceNo;
    private BigDecimal hydris;
    private Boolean isProbeDipped;
    private Boolean isHydrogenBw80And100;
    private BigDecimal nitrogen;
    private BigDecimal oxygen;
    private Integer noOfPrimeBlooms;
    private BigDecimal primeBloomsLength;
    private BigDecimal primeBloomsTotalLength;
    private Integer noOfCoBlooms;
    private BigDecimal coBloomsLength;
    private BigDecimal coBloomsTotalLength;
    private Integer noOfRejectedBlooms;
    private BigDecimal rejectedBloomsLength;
    private BigDecimal rejectedBloomsTotalLength;
    private BigDecimal weightOfPrimeBlooms;
    private BigDecimal weightOfCoBlooms;
    private BigDecimal weightOfRejectedBlooms;
    private BigDecimal totalCastWt;
    private String sentToLadle;
    private String heatRemark;
    private Boolean isDiverted;
}
