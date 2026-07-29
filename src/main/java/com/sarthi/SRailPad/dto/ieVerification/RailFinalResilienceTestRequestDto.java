package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailFinalResilienceTestRequestDto {
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private LocalDate dateOfShift;

    // Sample 1
    private String s1Impact1;
    private String s1Impact2;
    private String s1Impact3;
    private String s1Impact4;
    private String s1Impact5;
    private String s1Impact6;

    // Sample 2
    private String s2Impact1;
    private String s2Impact2;
    private String s2Impact3;
    private String s2Impact4;
    private String s2Impact5;
    private String s2Impact6;

    // Sample 3
    private String s3Impact1;
    private String s3Impact2;
    private String s3Impact3;
    private String s3Impact4;
    private String s3Impact5;
    private String s3Impact6;

    private String resilienceStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
