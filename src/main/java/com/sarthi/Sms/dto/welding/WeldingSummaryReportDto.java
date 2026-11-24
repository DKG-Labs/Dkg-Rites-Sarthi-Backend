package com.sarthi.Sms.dto.welding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeldingSummaryReportDto {
    private String date;
    private String railSection;
    private String railGrade;
    private String inspectionWorkspot;
    private String weldingMachineNumber;
    private Integer numberOfJointsOfferedForInspection;
    private Integer numberOfPanelsOfferedForInspection;
    private Integer dimensionalCheckAccepted;
    private Integer dimensionalCheckRejected;
    private Integer utCheckAccepted;
    private Integer utCheckRejected;
    private Integer numberOfJointsAccepted;
    private Integer numberOfJointsRejected;
    private Integer numberOfPanelsAccepted;
    private Integer numberOfPanelsRejected;
    private String railLength;
}
