package com.sarthi.Sms.dto.qct;

import java.util.List;

import lombok.Data;

@Data
public class QctFcgrDto {
    private String dutyId;
    private String remarks;
    private List<QctFcgrDtlDto> dtlList;
}
