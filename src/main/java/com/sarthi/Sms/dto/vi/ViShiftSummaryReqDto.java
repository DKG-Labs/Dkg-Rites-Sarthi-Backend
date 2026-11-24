package com.sarthi.Sms.dto.vi;

import lombok.Data;

@Data
public class ViShiftSummaryReqDto {
    private String date;        // Date in YYYY-MM-DD format
    private String shift;       // Shift (A/B/C)
    private Integer lineNumber; // Optional - specific line number, null for all lines
    private String mill;        // Optional - specific mill, null for all mills
}
