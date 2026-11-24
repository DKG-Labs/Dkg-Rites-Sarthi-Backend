package com.sarthi.Sms.dto.iso;

import java.math.BigDecimal;
import java.sql.Time;

import lombok.Data;

@Data
public class RollingControlHeatDetailDto {
    private String heatNumber;
    private Integer sampleNumber;
    private Time timing;
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
