package com.sarthi.Sms.dto.ndt;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NdtCalibrationResponseDto {

    private Integer calibrationId; // Unique identifier for the calibration
    private String dutyId; // Associated duty ID
    private String calibrationCompletionTime; // Time when the calibration was completed
    private BigDecimal calibrationSpeed; // Speed recorded during calibration
    private String rclRepName; // Name of the representative conducting the calibration
    private String calibrationRoundRemarks;
    private Boolean ut ;
    private Boolean ct;
    private Boolean fmg;
    private Boolean osiris;// Remarks for the calibration round

}
