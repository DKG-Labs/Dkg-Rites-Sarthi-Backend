package com.sarthi.Sms.dto.sms;

import lombok.Data;

@Data
public class ShiftSummaryReqDto {
    private Boolean emsFunctioning;
    private Boolean slagDetectorFunctioning;
    private Boolean amlcFunctioning;
    private Boolean hydrogenMeasurementAutomatic;
    private Boolean ladleToTundishUsed;
    private Boolean tundishToMouldUsed;
    private String makeOfCastingPowder;
    private String makeOfHydrisProbe;
    private String dutyId;
}
