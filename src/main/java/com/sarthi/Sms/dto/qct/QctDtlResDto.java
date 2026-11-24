package com.sarthi.Sms.dto.qct;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QctDtlResDto {
    private String heatNo;
    private Integer strandNo;
    private String sampleId;
    private String mill;
    private String railGrade;
    private String railSection;
    private String sampleMonth;
    private String qctType;
    private Integer qctId;
}
