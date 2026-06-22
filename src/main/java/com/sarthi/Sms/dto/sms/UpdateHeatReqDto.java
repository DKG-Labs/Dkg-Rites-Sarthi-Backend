package com.sarthi.Sms.dto.sms;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateHeatReqDto {
    private String dutyId;
    private String heatNo;
    private Integer turnDownTemp;
    private String turnDownTempWv;
    private String degassingVacuumWv;
    private String degassingDurationWv;
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
    private String sentToLadle;
    private Integer noOfPrimeBlooms;
    private BigDecimal primeBloomsLength;
    private BigDecimal primeBloomsTotalLength;
    private BigDecimal weightOfPrimeBlooms;
    private Integer noOfCoBlooms;
    private BigDecimal coBloomsLength;
    private BigDecimal coBloomsTotalLength;
    private BigDecimal weightOfCoBlooms;
    private Integer noOfRejectedBlooms;
    private BigDecimal rejectedBloomsLength;
    private BigDecimal rejectedBloomsTotalLength;
    private BigDecimal weightOfRejectedBlooms;
    private BigDecimal totalCastWt;
    private Boolean isDiverted;
    private String heatRemark;
    private String otherRemark;
}
