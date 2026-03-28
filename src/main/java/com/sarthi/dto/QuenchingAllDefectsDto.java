package com.sarthi.dto;

import lombok.Data;

@Data
public class QuenchingAllDefectsDto {

    private Integer quenchingTemperatureRejected;
    private Integer quenchingDurationRejected;
   private Integer quenchingHardnessRejected;
   private Integer boxGaugeRejected;
   private Integer flatBearingAreaRejected;
}
