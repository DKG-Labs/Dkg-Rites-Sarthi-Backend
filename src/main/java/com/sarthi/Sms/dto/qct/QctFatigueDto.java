package com.sarthi.Sms.dto.qct;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class QctFatigueDto {
    private String dutyId;
    private BigDecimal strain;
    private Integer frequency;
    private BigDecimal stress;
    private BigDecimal cyclesEndured;
    private String remarks;

    List<QctFatigueHeatDtlDto> dtlList;
}
