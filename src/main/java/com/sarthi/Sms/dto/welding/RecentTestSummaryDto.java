package com.sarthi.Sms.dto.welding;

import lombok.Data;

@Data
public class RecentTestSummaryDto {
    private String date;
    private String shift;
    private String testType;
    private String jointNo;
    private Long counter;
}
