// package com.rites.ritesbackend.dto.common;

// import lombok.Data;

// @Data
// public class StartDutyReqDto {
//     private String startDate;
//     private String shift;
//     private String sms;
//     private String railGrade;
//     private String mill;
//     private String railSection;
//     private String ndt;
// }


package com.sarthi.Sms.dto.sms.common;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StartDutyReqDto {
    private String startDate;
    private String shift;
    private String sms;
    private String railGrade;
    private String mill;
    private String railSection;
    private Integer lineNo;
    private BigDecimal stdOffLength;
    private String ndt;
    private String ieName1;
    private String ieName2;
    private String ieName3;
    private String rclIeName1;
    private String rclIeName2;
    private String rclIeName3;
    private String weldingLine;
    private String qct;
} 
