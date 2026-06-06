package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RailFinalNcrNylonCordTestResponseDto {
    private Long id;
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private LocalDate dateOfShift;

    private String s1Denier;
    private String s1Epi;
    private String s1Thickness;
    private String s1LoadAtBreak;
    private String s1Elongation;
    private String s1Twists;
    private String s2Denier;
    private String s2Epi;
    private String s2Thickness;
    private String s2LoadAtBreak;
    private String s2Elongation;
    private String s2Twists;
    private String s3Denier;
    private String s3Epi;
    private String s3Thickness;
    private String s3LoadAtBreak;
    private String s3Elongation;
    private String s3Twists;
    private String m1Denier;
    private String m1Epi;
    private String m1Thickness;
    private String m1LoadAtBreak;
    private String m1Elongation;
    private String m1Twists;
    private String m2Denier;
    private String m2Epi;
    private String m2Thickness;
    private String m2LoadAtBreak;
    private String m2Elongation;
    private String m2Twists;
    private String m3Denier;
    private String m3Epi;
    private String m3Thickness;
    private String m3LoadAtBreak;
    private String m3Elongation;
    private String m3Twists;
    private String m4Denier;
    private String m4Epi;
    private String m4Thickness;
    private String m4LoadAtBreak;
    private String m4Elongation;
    private String m4Twists;
    private String m5Denier;
    private String m5Epi;
    private String m5Thickness;
    private String m5LoadAtBreak;
    private String m5Elongation;
    private String m5Twists;
    private String m6Denier;
    private String m6Epi;
    private String m6Thickness;
    private String m6LoadAtBreak;
    private String m6Elongation;
    private String m6Twists;

    private String ncrCordStatus;
    private Integer notOkCount;
    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
