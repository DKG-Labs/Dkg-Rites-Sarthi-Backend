package com.sarthi.Sms.dto.rolling;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ControlHeatReqDto {
    private String dutyId;
    private String heatNo;
    private Integer sampleNo;
    private String timing;
    private String sampleLocation;
    private BigDecimal height;
    private String head;
    private BigDecimal flange;
    private BigDecimal web;
    private String asy;
    private String footToe;
    private String fishingHeight;
    private BigDecimal weight;
    private String crownProfile;
    private String footFlatness;
    private String remark;
}
