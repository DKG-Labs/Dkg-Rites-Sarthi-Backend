package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailFinalPeriodicTgaRequestDto {
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private LocalDate dateOfShift;

    private LocalDate dateOfLastTest;
    private Integer qtyProducedSinceLastTest;
    private Integer testingThreshold;
    private Boolean isMandatory;

    // Sample 1
    private String s1LotNo;
    private String s1SampleNo;
    private String s1SampleWt;
    private String s1TempRange;
    private String s1PolymerContent;

    // Sample 2
    private String s2LotNo;
    private String s2SampleNo;
    private String s2SampleWt;
    private String s2TempRange;
    private String s2PolymerContent;

    // Sample 3
    private String s3LotNo;
    private String s3SampleNo;
    private String s3SampleWt;
    private String s3TempRange;
    private String s3PolymerContent;

    // Sample 4
    private String s4LotNo;
    private String s4SampleNo;
    private String s4SampleWt;
    private String s4TempRange;
    private String s4PolymerContent;

    // Sample 5
    private String s5LotNo;
    private String s5SampleNo;
    private String s5SampleWt;
    private String s5TempRange;
    private String s5PolymerContent;

    private String tgaStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
