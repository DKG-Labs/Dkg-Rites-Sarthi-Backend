package com.sarthi.Sms.dto.rolling;

import lombok.Data;

@Data
public class ControlReqDto {
    private String dutyId;
    private String micrometerNo;
    private String vernierNo;
    private String weighingMachine;
    private String micrometerValidity;
    private String vernierValidity;
    private String weighingMachineValidity;
    private String branding;
    private Integer noOfGauges;
}
