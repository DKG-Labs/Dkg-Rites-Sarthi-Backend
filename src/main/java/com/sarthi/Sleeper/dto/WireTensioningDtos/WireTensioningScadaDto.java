package com.sarthi.Sleeper.dto.WireTensioningDtos;

import lombok.Data;

@Data
public class WireTensioningScadaDto {

    private Long id;

    private String plcTime;
    private String benchNo;

    private Double wireLength;
    private Double crossSection;
    private Double youngsModulus;

    private Double measuredElongation;
    private Double forceElongation;

    private Double totalLoad;
    private Double finalLoad;
}