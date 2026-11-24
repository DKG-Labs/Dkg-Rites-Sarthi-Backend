package com.sarthi.Sms.dto.rolling;

import java.util.List;

import lombok.Data;

@Data
public class HtSequenceResDto {
    private String heatNo;
    List<HtSequenceRailIdDto> railIdDtlList;
}
