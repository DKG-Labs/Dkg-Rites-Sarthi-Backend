package com.sarthi.Sms.dto.sms;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SmsHeatReportDto {

    private String heatNo;
    private String smsNumber;
    private String casterNumber;
    private String railGrade;
    private String dateAndShiftOfCasting;
    private String sequenceNo;
    private Integer turnDownTemp;
    private BigDecimal degassingVacuum;
    private BigDecimal degassingDuration;
    private Integer castingTemp;
    private BigDecimal hydrogen;
    private BigDecimal nitrogen;
    private BigDecimal oxygen;
    private String chemical;
    private Integer noOfPrimeBlooms;
    private Integer noOfCoBlooms;
    private Integer noOfRejectedBlooms;
    private BigDecimal totalCastWt;
    private String heatRemark;
    private String reasonForRejection;
    private String probeMakeName;
    private Boolean isHydrisMeasuredBw80To100mOfCasting;
    private Boolean isProbeDippedBelow300mmFromSlagMetalInterface;
    private String makeOfCastingPowder;
    private Boolean isEmsFunctioning;
    private Boolean isSlagDetectorFunctioning;
    private Boolean isAmlcFunctioning;
    private Boolean isHydrogenMeasurementAutomatic;
    private Boolean isLadleToTundishUsed;
    private Boolean isTundishToMouldUsed;
}
