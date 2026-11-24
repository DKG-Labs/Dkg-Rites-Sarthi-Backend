package com.sarthi.Sms.dto.rolling;

import lombok.Data;

@Data
public class RollingHeatResDto {
    private String heatNo;
    private Integer sampleNo;
    private String timing;
    private String result;
}
