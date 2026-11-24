package com.sarthi.Sms.dto.qct;

import java.util.List;

import lombok.Data;

@Data
public class QctCenterLineDto {
    private String dutyId;
    private String remarks;
    private List<QctCenterLineDtlDto> dtlList;
}
