package com.sarthi.Sms.dto.sms;


import com.sarthi.Sms.entity.sms.HeatDtlEntity;
import com.sarthi.Sms.entity.sms.HeatDtlSms2Entity;
import com.sarthi.Sms.entity.sms.HeatDtlSms3Entity;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShiftSummaryHeatDtlDto {
    private String heatNo;
    private String sequenceNo;
    private String heatRemark;
    private Boolean isDiverted;
    private BigDecimal hydris;
    private String heatStage;

    public ShiftSummaryHeatDtlDto(HeatDtlEntity heatDtlEntity){
        this.heatNo = heatDtlEntity.getHeatNo();
        this.sequenceNo = heatDtlEntity.getSequenceNo();
        this.heatRemark = heatDtlEntity.getHeatRemark();
        this.isDiverted = heatDtlEntity.getIsDiverted();
        this.hydris = heatDtlEntity.getHydris();
        this.heatStage = heatDtlEntity.getHeatStage();
    }
    public ShiftSummaryHeatDtlDto(HeatDtlSms2Entity heatDtlEntity){
        this.heatNo = heatDtlEntity.getHeatNo();
        this.sequenceNo = heatDtlEntity.getSequenceNo();
        this.heatRemark = heatDtlEntity.getHeatRemark();
        this.isDiverted = heatDtlEntity.getIsDiverted();
        this.hydris = heatDtlEntity.getHydris();
        this.heatStage = heatDtlEntity.getHeatStage();
    }
    public ShiftSummaryHeatDtlDto(HeatDtlSms3Entity heatDtlEntity){
        this.heatNo = heatDtlEntity.getHeatNo();
        this.sequenceNo = heatDtlEntity.getSequenceNo();
        this.heatRemark = heatDtlEntity.getHeatRemark();
        this.isDiverted = heatDtlEntity.getIsDiverted();
        this.hydris = heatDtlEntity.getHydris();
        this.heatStage = heatDtlEntity.getHeatStage();
    }
}
