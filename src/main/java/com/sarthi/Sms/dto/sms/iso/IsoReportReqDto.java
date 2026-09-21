package com.sarthi.Sms.dto.sms.iso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IsoReportReqDto {
    private String date;
    private String shift;
    private String railGrade;
    private String railSection;
    private String sms;
}
