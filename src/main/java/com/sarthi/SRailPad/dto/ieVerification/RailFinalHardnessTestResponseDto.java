package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RailFinalHardnessTestResponseDto {
    private Long id;
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private java.time.LocalDate dateOfShift;

    // Compound A samples (1 to 5)
    private String sampleA1;
    private String sampleA2;
    private String sampleA3;
    private String sampleA4;
    private String sampleA5;

    // Compound A marginal samples (1 to 10)
    private String marginalA1;
    private String marginalA2;
    private String marginalA3;
    private String marginalA4;
    private String marginalA5;
    private String marginalA6;
    private String marginalA7;
    private String marginalA8;
    private String marginalA9;
    private String marginalA10;

    // Compound B samples (1 to 5)
    private String sampleB1;
    private String sampleB2;
    private String sampleB3;
    private String sampleB4;
    private String sampleB5;

    // Compound B marginal samples (1 to 10)
    private String marginalB1;
    private String marginalB2;
    private String marginalB3;
    private String marginalB4;
    private String marginalB5;
    private String marginalB6;
    private String marginalB7;
    private String marginalB8;
    private String marginalB9;
    private String marginalB10;

    private String hardnessStatus;
    private Integer notOkCount;
    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;
    private Long updatedBy;
    private LocalDateTime updatedDate;
}
