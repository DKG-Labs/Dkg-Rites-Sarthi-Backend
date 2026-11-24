package com.sarthi.Sms.dto.qct;

import java.util.List;

import lombok.Data;

@Data
public class QctResidualDto {
    private String remarks;
    private String dutyId;
    private List<QctResidualDtlDto> dtlList;
}
