package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;

@Data
public class RailFinalModulusRequestDto {
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private java.time.LocalDate dateOfShift;

    // Before Ageing actual samples (1 to 3)
    private String sampleBefore1;
    private String sampleBefore2;
    private String sampleBefore3;

    // Before Ageing marginal samples (1 to 6)
    private String marginalBefore1;
    private String marginalBefore2;
    private String marginalBefore3;
    private String marginalBefore4;
    private String marginalBefore5;
    private String marginalBefore6;

    // After Ageing actual samples (1 to 3)
    private String sampleAfter1;
    private String sampleAfter2;
    private String sampleAfter3;

    // After Ageing marginal samples (1 to 6)
    private String marginalAfter1;
    private String marginalAfter2;
    private String marginalAfter3;
    private String marginalAfter4;
    private String marginalAfter5;
    private String marginalAfter6;

    private String modulusStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
