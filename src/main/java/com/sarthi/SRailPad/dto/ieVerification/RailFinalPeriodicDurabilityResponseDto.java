package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RailFinalPeriodicDurabilityResponseDto {
    private Long id;
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
    private String s1InitialThickness;
    private String s1FinalThickness;
    private String s1ReductionThickness;
    private String s1InitialLoadComp;
    private String s1FinalLoadComp;
    private String s1ChangeLd;

    // Sample 2
    private String s2LotNo;
    private String s2InitialThickness;
    private String s2FinalThickness;
    private String s2ReductionThickness;
    private String s2InitialLoadComp;
    private String s2FinalLoadComp;
    private String s2ChangeLd;

    // Sample 3
    private String s3LotNo;
    private String s3InitialThickness;
    private String s3FinalThickness;
    private String s3ReductionThickness;
    private String s3InitialLoadComp;
    private String s3FinalLoadComp;
    private String s3ChangeLd;

    // Sample 4
    private String s4LotNo;
    private String s4InitialThickness;
    private String s4FinalThickness;
    private String s4ReductionThickness;
    private String s4InitialLoadComp;
    private String s4FinalLoadComp;
    private String s4ChangeLd;

    // Sample 5
    private String s5LotNo;
    private String s5InitialThickness;
    private String s5FinalThickness;
    private String s5ReductionThickness;
    private String s5InitialLoadComp;
    private String s5FinalLoadComp;
    private String s5ChangeLd;

    private String durabilityStatus;
    private Integer notOkCount;
    private String remarks;

    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
