package com.sarthi.Sms.dto.ndt;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalibrationListDto {
    private String calibrationCompletionTime;
    private BigDecimal calibrationSpeed;
    private String rclRepName;
    private String calibrationRoundRemarks;
    private Boolean ut ;
    private Boolean ct;
    private Boolean fmg;
    private Boolean osiris;
}
