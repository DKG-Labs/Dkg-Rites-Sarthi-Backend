package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RailFinalElongationResponseDto {
    private Long id;
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private java.time.LocalDate dateOfShift;

    // Before Ageing actual samples (1 to 5)
    private String sampleBefore1;
    private String sampleBefore2;
    private String sampleBefore3;
    private String sampleBefore4;
    private String sampleBefore5;

    // Before Ageing marginal samples (1 to 10)
    private String marginalBefore1;
    private String marginalBefore2;
    private String marginalBefore3;
    private String marginalBefore4;
    private String marginalBefore5;
    private String marginalBefore6;
    private String marginalBefore7;
    private String marginalBefore8;
    private String marginalBefore9;
    private String marginalBefore10;

    // After Ageing actual samples (1 to 5)
    private String sampleAfter1;
    private String sampleAfter2;
    private String sampleAfter3;
    private String sampleAfter4;
    private String sampleAfter5;

    // After Ageing marginal samples (1 to 10)
    private String marginalAfter1;
    private String marginalAfter2;
    private String marginalAfter3;
    private String marginalAfter4;
    private String marginalAfter5;
    private String marginalAfter6;
    private String marginalAfter7;
    private String marginalAfter8;
    private String marginalAfter9;
    private String marginalAfter10;

    private String elongationStatus;
    private Integer notOkCount;
    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
