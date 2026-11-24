package com.sarthi.Sms.dto.ndt;

import lombok.Data;
import java.util.List;

@Data
public class NdtCalibrationListResponseDto {
    private String dutyId; // The duty ID for which calibration data is fetched
    private List<NdtCalibrationResponseDto> calibrations; // List of calibration details
}
