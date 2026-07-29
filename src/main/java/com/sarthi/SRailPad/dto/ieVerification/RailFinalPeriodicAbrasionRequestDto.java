package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RailFinalPeriodicAbrasionRequestDto {
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
    private String s1InitialMass;
    private String s1FinalMass;
    private String s1LossOfMass;
    private String s1RelativeLoss;

    // Sample 2
    private String s2LotNo;
    private String s2SampleNo;
    private String s2InitialMass;
    private String s2FinalMass;
    private String s2LossOfMass;
    private String s2RelativeLoss;

    // Sample 3
    private String s3LotNo;
    private String s3SampleNo;
    private String s3InitialMass;
    private String s3FinalMass;
    private String s3LossOfMass;
    private String s3RelativeLoss;

    // Sample 4
    private String s4LotNo;
    private String s4SampleNo;
    private String s4InitialMass;
    private String s4FinalMass;
    private String s4LossOfMass;
    private String s4RelativeLoss;

    // Sample 5
    private String s5LotNo;
    private String s5SampleNo;
    private String s5InitialMass;
    private String s5FinalMass;
    private String s5LossOfMass;
    private String s5RelativeLoss;

    private String abrasionStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
