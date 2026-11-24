package com.sarthi.Sms.dto.dashboard;

import lombok.Data;

@Data
public class DimensionalInspectionReqDto {
    private String startDate;
    private String endDate;
    private String shift;
}
