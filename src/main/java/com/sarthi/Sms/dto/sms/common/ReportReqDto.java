package com.sarthi.Sms.dto.sms.common;

import lombok.Data;

@Data
public class ReportReqDto {
    private String startDate;
    private String endDate;
    private String shift;
    private String lineNo; // Optional filter for line number

    // Enhanced shift-specific date range filtering
    private String startShift; // Shift for start date (A/B/C)
    private String endShift;   // Shift for end date (A/B/C)
}
