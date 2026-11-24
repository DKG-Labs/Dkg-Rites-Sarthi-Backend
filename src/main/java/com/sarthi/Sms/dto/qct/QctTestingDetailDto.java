package com.sarthi.Sms.dto.qct;

import lombok.Data;

@Data
public class QctTestingDetailDto {
    private int heatNo;
    private int strandNo;
    private String dutyId;
    private String sampleId;
    private String qctType;
    private String fatiqueResult;
    private float stressMpa;
    private String natureOfStress;
    private Float fcgrDaDn10;
    private Float fcgrDaDn13_5;
    private String valueReportedAs;
    private Float valueOfReportedParameter;
    private Integer noOfHardness;
    private Float meanRsh;
    private Float mxVariation;
}
