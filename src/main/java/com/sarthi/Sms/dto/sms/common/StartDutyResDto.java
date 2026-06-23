package com.sarthi.Sms.dto.sms.common;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StartDutyResDto {
    private String date;
    private String startTime;
    private String sms;
    private String shift;
    private String dutyId;
    private String mill;
    private String railSection;
    private String railGrade;
    private String ndt;
    private Integer lineNo;
    private BigDecimal stdOffLength;
    private String ieName1;
    private String ieName2;
    private String ieName3;
    private String rclIeName1;
    private String rclIeName2;
    private String rclIeName3;
    private String weldingLine;
    private String qct;
}
