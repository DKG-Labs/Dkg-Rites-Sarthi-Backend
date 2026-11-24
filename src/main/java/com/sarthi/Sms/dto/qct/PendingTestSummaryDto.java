package com.sarthi.Sms.dto.qct;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class PendingTestSummaryDto {
    private String qctType;
    private String mill;
    private String railGrade;
    private String railSection;
    private String sampleMonth;
    private String sampleCount;
}
