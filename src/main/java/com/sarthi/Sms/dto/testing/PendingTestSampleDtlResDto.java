package com.sarthi.Sms.dto.testing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingTestSampleDtlResDto {
    // private String heatNo;
    // private Integer strand;
    // private String mill;
    // private String railGrade;
    // private String sampleType;
    // private String sampleLot;
    // private String sampleId;
    // private String testsMarked;
    // private String testMarkedStatus;  // New field

    private String heatNo;
    private Integer strand;
    private String mill;
    private String railGrade;
    private String sampleType;
    private String sampleLot;
    private String sampleId;
    private String testsMarked;
    private String testStatuses;
}
