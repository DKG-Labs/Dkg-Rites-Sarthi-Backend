package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailFinalSecantStiffnessTestRequestDto {
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private LocalDate dateOfShift;

    private String s1S20A;
    private String s1S20B;
    private String s1S20C;
    private String s1S20D;
    private String s1S90A;
    private String s1S90B;
    private String s1S90C;
    private String s1S90D;
    private String s2S20A;
    private String s2S20B;
    private String s2S20C;
    private String s2S20D;
    private String s2S90A;
    private String s2S90B;
    private String s2S90C;
    private String s2S90D;
    private String m1S20A;
    private String m1S20B;
    private String m1S20C;
    private String m1S20D;
    private String m1S90A;
    private String m1S90B;
    private String m1S90C;
    private String m1S90D;
    private String m2S20A;
    private String m2S20B;
    private String m2S20C;
    private String m2S20D;
    private String m2S90A;
    private String m2S90B;
    private String m2S90C;
    private String m2S90D;
    private String m3S20A;
    private String m3S20B;
    private String m3S20C;
    private String m3S20D;
    private String m3S90A;
    private String m3S90B;
    private String m3S90C;
    private String m3S90D;
    private String m4S20A;
    private String m4S20B;
    private String m4S20C;
    private String m4S20D;
    private String m4S90A;
    private String m4S90B;
    private String m4S90C;
    private String m4S90D;

    private String secantStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
