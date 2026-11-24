package com.sarthi.Sms.dto.rolling;

import lombok.Data;

@Data
public class HtSequenceReqDto {
    private String dutyId;
    private String railId;
    private String bloomQuality;
    private Boolean htStatus;
}
