package com.sarthi.Sleeper.dto.HtsWire;

import lombok.Data;

@Data
public class HtsCoilDetailsResponseDto {

    private Long id;

    private String coilFrom;
    private String coilTo;
    private String lotNo;
    private String coilNo;
    private Double qtyKg;
    private String entryType;
}