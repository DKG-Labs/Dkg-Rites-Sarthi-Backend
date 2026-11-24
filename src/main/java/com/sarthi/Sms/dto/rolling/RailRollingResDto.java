package com.sarthi.Sms.dto.rolling;

import java.util.List;

import lombok.Data;

@Data
public class RailRollingResDto {
    private String micrometerNo;
    private String vernierNo;
    private String weighingMachine;
    private String micrometerValidity;
    private String vernierValidity;
    private String weighingMachineValidity;
    private String branding;
    private Integer noOfGauges;
    private List<RollingHeatResDto> railRollingHeatList;
}
