package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;

@Data
public class RailFinalLoadTestRequestDto {
    private String callNo;
    private String lotNo;
    private String plantId;
    private String vendorCode;
    private String shift;
    private String railpadType;
    private Integer offeredQty;
    private java.time.LocalDate dateOfShift;

    // Load Tonnes (Rows 1 to 8)
    private String load1; private String load2; private String load3; private String load4;
    private String load5; private String load6; private String load7; private String load8;

    // Pad 1 L & R
    private String pad1L1; private String pad1L2; private String pad1L3; private String pad1L4;
    private String pad1L5; private String pad1L6; private String pad1L7; private String pad1L8;
    private String pad1R1; private String pad1R2; private String pad1R3; private String pad1R4;
    private String pad1R5; private String pad1R6; private String pad1R7; private String pad1R8;

    // Pad 2 L & R
    private String pad2L1; private String pad2L2; private String pad2L3; private String pad2L4;
    private String pad2L5; private String pad2L6; private String pad2L7; private String pad2L8;
    private String pad2R1; private String pad2R2; private String pad2R3; private String pad2R4;
    private String pad2R5; private String pad2R6; private String pad2R7; private String pad2R8;

    // Marginal Pad 1 L & R
    private String mPad1L1; private String mPad1L2; private String mPad1L3; private String mPad1L4;
    private String mPad1L5; private String mPad1L6; private String mPad1L7; private String mPad1L8;
    private String mPad1R1; private String mPad1R2; private String mPad1R3; private String mPad1R4;
    private String mPad1R5; private String mPad1R6; private String mPad1R7; private String mPad1R8;

    // Marginal Pad 2 L & R
    private String mPad2L1; private String mPad2L2; private String mPad2L3; private String mPad2L4;
    private String mPad2L5; private String mPad2L6; private String mPad2L7; private String mPad2L8;
    private String mPad2R1; private String mPad2R2; private String mPad2R3; private String mPad2R4;
    private String mPad2R5; private String mPad2R6; private String mPad2R7; private String mPad2R8;

    // Marginal Pad 3 L & R
    private String mPad3L1; private String mPad3L2; private String mPad3L3; private String mPad3L4;
    private String mPad3L5; private String mPad3L6; private String mPad3L7; private String mPad3L8;
    private String mPad3R1; private String mPad3R2; private String mPad3R3; private String mPad3R4;
    private String mPad3R5; private String mPad3R6; private String mPad3R7; private String mPad3R8;

    // Marginal Pad 4 L & R
    private String mPad4L1; private String mPad4L2; private String mPad4L3; private String mPad4L4;
    private String mPad4L5; private String mPad4L6; private String mPad4L7; private String mPad4L8;
    private String mPad4R1; private String mPad4R2; private String mPad4R3; private String mPad4R4;
    private String mPad4R5; private String mPad4R6; private String mPad4R7; private String mPad4R8;

    private String loadStatus;
    private Integer notOkCount;
    private String remarks;
    private Long userId;
}
