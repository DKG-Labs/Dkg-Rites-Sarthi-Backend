package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailFinalNcrAdhesionTestRequestDto {
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private LocalDate dateOfShift;

    private String s1Peel;
    private String s1Hpull;
    private String s2Peel;
    private String s2Hpull;
    private String m1Peel;
    private String m1Hpull;
    private String m2Peel;
    private String m2Hpull;
    private String m3Peel;
    private String m3Hpull;
    private String m4Peel;
    private String m4Hpull;

    private String ncrAdhesionStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
