package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;

@Data
public class RailFinalElectricalResistanceRequestDto {
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private java.time.LocalDate dateOfShift;

    // Before Immersion Forward (Actual s1..3, Marginal m1..6)
    private String s1BeforeForward;
    private String s2BeforeForward;
    private String s3BeforeForward;
    private String m1BeforeForward;
    private String m2BeforeForward;
    private String m3BeforeForward;
    private String m4BeforeForward;
    private String m5BeforeForward;
    private String m6BeforeForward;

    // Before Immersion Reverse (Actual s1..3, Marginal m1..6)
    private String s1BeforeReverse;
    private String s2BeforeReverse;
    private String s3BeforeReverse;
    private String m1BeforeReverse;
    private String m2BeforeReverse;
    private String m3BeforeReverse;
    private String m4BeforeReverse;
    private String m5BeforeReverse;
    private String m6BeforeReverse;

    // After Immersion Forward (Actual s1..3, Marginal m1..6)
    private String s1AfterForward;
    private String s2AfterForward;
    private String s3AfterForward;
    private String m1AfterForward;
    private String m2AfterForward;
    private String m3AfterForward;
    private String m4AfterForward;
    private String m5AfterForward;
    private String m6AfterForward;

    // After Immersion Reverse (Actual s1..3, Marginal m1..6)
    private String s1AfterReverse;
    private String s2AfterReverse;
    private String s3AfterReverse;
    private String m1AfterReverse;
    private String m2AfterReverse;
    private String m3AfterReverse;
    private String m4AfterReverse;
    private String m5AfterReverse;
    private String m6AfterReverse;

    private String electricalStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
