package com.sarthi.Sms.dto.vi;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcptDataReportDto {
    private String railId;
    private String heatNumber;
    private String dateShift;
    private String railGrade;
    private String railSection;
    private String inspectionWorkspot;
    private BigDecimal standardOfferedLength;
    private BigDecimal actualOfferedLength;
    private String acceptanceInClassA;
    private String acceptanceInClassAPlus01;
    private String acceptanceInClassB;
    private String acceptedIds;
    private String rejectedLength;
    private String reasonForDefect;

}
