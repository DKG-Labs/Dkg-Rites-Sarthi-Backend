package com.sarthi.Sms.dto.qct;

import lombok.Data;

import java.util.List;

@Data
public class GetQctInspectionResDto {
    private String dutyId;
    private String mill;
    private String railSection;
    private String railGrade;
    private String monthYear;
    private String qctType;
    private int noOfSamples;
    private String remark;
    private List<QctNewTestSampleDto> details;
}

