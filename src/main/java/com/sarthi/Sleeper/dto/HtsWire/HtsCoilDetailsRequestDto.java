package com.sarthi.Sleeper.dto.HtsWire;

import lombok.Data;

@Data
public class HtsCoilDetailsRequestDto {

    private String coilFrom;
    private String coilTo;
    private String coilNo;
    private String lotNo;
    private Double qtyKg;
    private String entryType; // SINGLE / RANGE
}