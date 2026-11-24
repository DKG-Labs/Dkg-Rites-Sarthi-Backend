package com.sarthi.Sms.dto.ndt;

import java.util.List;

import lombok.Data;

@Data
public class NdtCalibrationDto {
    private String calibrationRemarks;
    private String dutyId;
    private List<CalibrationListDto> calibrationList;
}
