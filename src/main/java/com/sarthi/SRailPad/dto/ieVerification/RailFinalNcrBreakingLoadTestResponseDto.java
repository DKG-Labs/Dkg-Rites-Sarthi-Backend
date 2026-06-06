package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RailFinalNcrBreakingLoadTestResponseDto {
    private Long id;
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private LocalDate dateOfShift;

    private String sample1;
    private String sample2;
    private String sample3;
    private String sample4;
    private String sample5;
    private String marginal1;
    private String marginal2;
    private String marginal3;
    private String marginal4;
    private String marginal5;
    private String marginal6;
    private String marginal7;
    private String marginal8;
    private String marginal9;
    private String marginal10;

    private String ncrBreakingStatus;
    private Integer notOkCount;
    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
