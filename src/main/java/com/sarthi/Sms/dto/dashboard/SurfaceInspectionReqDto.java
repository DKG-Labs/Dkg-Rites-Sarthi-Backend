package com.sarthi.Sms.dto.dashboard;

import lombok.Data;

@Data
public class SurfaceInspectionReqDto {
    private String startDate;
    private String endDate;
    private String shift;
}