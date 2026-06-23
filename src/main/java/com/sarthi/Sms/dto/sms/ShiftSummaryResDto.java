package com.sarthi.Sms.dto.sms;

import lombok.Data;

import java.util.List;

@Data
public class ShiftSummaryResDto {
    private List<ShiftSummaryHeatDtlDto> heatDtlList;
    private HydrisClbResDto hydrisClb;
    private List<LecoClbResDto> lecoClbList;
    private boolean isEmsFunctioning;
    private boolean isSlagDetectorFunctioning;
    private boolean isAmlcFunctioning;
    private boolean isHydrogenMeasurementAutomatic;
    private boolean isLadleToTundishUsed;
    private boolean isTundishToMouldUsed;
    private String makeOfCastingPowder;
    private String makeOfHydrisProbe;
    private String dutyId;
}
