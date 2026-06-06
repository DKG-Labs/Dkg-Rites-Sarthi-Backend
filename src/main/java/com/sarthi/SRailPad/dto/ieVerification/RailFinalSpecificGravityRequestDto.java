package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;

@Data
public class RailFinalSpecificGravityRequestDto {
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private java.time.LocalDate dateOfShift;

    // Compound A - Air Weight (Actual s1..3, Marginal m1..6)
    private String s1AAir;
    private String s2AAir;
    private String s3AAir;
    private String m1AAir;
    private String m2AAir;
    private String m3AAir;
    private String m4AAir;
    private String m5AAir;
    private String m6AAir;

    // Compound A - Water Weight (Actual s1..3, Marginal m1..6)
    private String s1AWater;
    private String s2AWater;
    private String s3AWater;
    private String m1AWater;
    private String m2AWater;
    private String m3AWater;
    private String m4AWater;
    private String m5AWater;
    private String m6AWater;

    // Compound B - Air Weight (Actual s1..3, Marginal m1..6)
    private String s1BAir;
    private String s2BAir;
    private String s3BAir;
    private String m1BAir;
    private String m2BAir;
    private String m3BAir;
    private String m4BAir;
    private String m5BAir;
    private String m6BAir;

    // Compound B - Water Weight (Actual s1..3, Marginal m1..6)
    private String s1BWater;
    private String s2BWater;
    private String s3BWater;
    private String m1BWater;
    private String m2BWater;
    private String m3BWater;
    private String m4BWater;
    private String m5BWater;
    private String m6BWater;

    private String sgStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
