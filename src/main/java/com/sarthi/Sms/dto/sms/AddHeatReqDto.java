package com.sarthi.Sms.dto.sms;

import lombok.Data;

@Data
public class AddHeatReqDto {
    private String dutyId;
    private String heatNo;
    private Integer turnDownTemp;
    private String turnDownTempWv;
}
