package com.sarthi.Sms.dto.iso;

import java.util.List;

import lombok.Data;

@Data
public class RollingControlDto {
    private String dutyId; // To differentiate between multiple duty entries
    private String vernierNumber;
    private String micrometerNumber;
    private Integer numberOfGauges;
    private List<RollingControlHeatDetailDto> heatDtlList;
}
