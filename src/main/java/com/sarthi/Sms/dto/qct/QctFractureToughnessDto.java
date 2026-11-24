package com.sarthi.Sms.dto.qct;

import java.util.List;

import lombok.Data;

@Data
public class QctFractureToughnessDto {
    private String dutyId;
    private String remarks;
    private List<QctFractureToughnessDtlDto> dtlList;
}
