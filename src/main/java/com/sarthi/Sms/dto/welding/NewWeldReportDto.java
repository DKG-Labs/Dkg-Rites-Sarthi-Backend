package com.sarthi.Sms.dto.welding;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewWeldReportDto {
    private String date;
    private String shift;
    private String inspectionWorkspot;
    private String railSection;
    private String railGrade;
    private String panelId;
    private String jointNo;
    private String weldJointResult;
    private String weldJointReason;
    private String weldJointRemark;
    private String railId1;
    private String railId2;
    private Integer panelLength;
    private String panelDecision;
    private String panelRemark;
    private BigDecimal weldingRailLength1;
    private BigDecimal weldingRailLength2;

}
