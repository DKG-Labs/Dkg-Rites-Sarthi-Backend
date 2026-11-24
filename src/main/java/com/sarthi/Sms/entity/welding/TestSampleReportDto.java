package com.sarthi.Sms.entity.welding;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestSampleReportDto {
    private String mill;
    private String railSection;
    private String railGrade;
    private String inspectionWorkspot;
    private String weldingMachineNumber;
    private String tltJointNo;
    private String tltMarkingDateShift;
    private String tltTestingStatus;
    private String tltLoadDeflection;
    private String tltRemark;
    private String hardnessJointNo;
    private String hardnessMarkingDate;
    private String hardnessTestingStatus;
    private String hardnessDeviation;
    private String hardnessRemark;
    private String macroMicroJointNo;
    private String macroMicroMarkingDate;
    private String macroMicroTestingStatus;
    private String macroMicroHazFullRailFoot;
    private String macroMicroHazOneSideFoot;
    private String macroMicroRemark;
    private BigDecimal railLength1;
    private BigDecimal railLength2;
}
