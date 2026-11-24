package com.sarthi.Sms.dto.qct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QctRecordDto {
    private String mill;
    private String railGrade;
    private String railSection;
    private String heatNo;
    private Integer strandNo;
    private String sampleId;
    private String fatigueInfo;
    private String residualInfo;
    private String fcgrInfo;
    private String fractureToughnessInfo;
    private String centerlineInfo;
}
