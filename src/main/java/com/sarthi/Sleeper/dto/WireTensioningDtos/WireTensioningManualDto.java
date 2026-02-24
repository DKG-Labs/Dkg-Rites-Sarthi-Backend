package com.sarthi.Sleeper.dto.WireTensioningDtos;

import lombok.Data;

@Data
public class WireTensioningManualDto {

    private Long id;

    private String batchNo;
    private String benchNo;
    private String time;

    private Double wireLength;
    private Double crossSection;
    private Double youngsModulus;

    private Double measuredElongation;
    private Double forceElongation;

    private Double totalLoad;
    private Double finalLoad;
}
