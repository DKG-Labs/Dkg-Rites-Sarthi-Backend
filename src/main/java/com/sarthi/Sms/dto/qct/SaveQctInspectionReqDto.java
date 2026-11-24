package com.sarthi.Sms.dto.qct;

import lombok.Data;

import java.util.List;

@Data
public class SaveQctInspectionReqDto {
    private String dutyId;
    private String railGrade;
    private String railSection;
    private String mill;
    private String qctType;
    private int noOfSamples;
    private String monthYear;
    private String remark;
    private List<QctNewTestSampleDto> details; // List for storing QCT details.
}

