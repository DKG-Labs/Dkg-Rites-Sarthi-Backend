package com.sarthi.Sms.dto.rolling;

import lombok.Data;

@Data
public class HeatAcptTestDtlReqDto {
    private String sampleId;
    private String heatNo;
    private String sampleType;
    private Integer strand;
    private String sampleLot;
}
