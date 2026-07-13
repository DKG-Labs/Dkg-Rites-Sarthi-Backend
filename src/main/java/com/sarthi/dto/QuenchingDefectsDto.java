package com.sarthi.dto;

import lombok.Data;

@Data
public class QuenchingDefectsDto {

    private Integer quenchingHardness;
    private Integer boxGaugeRejected;
    private Integer flatBearingAreaRejected;
    private Integer fallingGaugeRejected;
}
