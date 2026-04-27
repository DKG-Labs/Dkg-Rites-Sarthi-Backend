package com.sarthi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PoInspection2ndLevelSerialStatusDto {

    private Integer slNo;

        private String rlyPoSrNo;     // rly + poNo + itemSrNo
    private String consignee;

    private LocalDateTime originalDpDate;
    private LocalDateTime extendedDpDate;

    private Integer poSrNoQty;
    private Integer balancePoQty;

    private Integer noOfIcIssued;

    private Double rawMaterialAcceptedMt;
    private Double rawMaterialRejectionPercentage;

    private Integer processInspectionMaterialAcceptedNos;
    private Double processInspectionMaterialRejectionPercentage;

    private Integer finalInspectionMaterialAcceptedNos;
    private Double finalInspectionMaterialRejectionPercentage;


    public PoInspection2ndLevelSerialStatusDto(
            Integer slNo,
            String rlyPoSrNo,
            String consignee,
            LocalDateTime originalDpDate,
            LocalDateTime extendedDpDate,
            Integer poSrNoQty,
            Integer balancePoQty,
            Integer noOfIcIssued,
            Double rawMaterialAcceptedMt,
            Double rawMaterialRejectionPercentage,
            Integer processInspectionMaterialAcceptedNos,
            Double processInspectionMaterialRejectionPercentage,
            Integer finalInspectionMaterialAcceptedNos,
            Double finalInspectionMaterialRejectionPercentage
    ) {
        this.slNo = slNo;
        this.rlyPoSrNo = rlyPoSrNo;
        this.consignee = consignee;
        this.originalDpDate = originalDpDate;
        this.extendedDpDate = extendedDpDate;
        this.poSrNoQty = poSrNoQty;
        this.balancePoQty = balancePoQty;
        this.noOfIcIssued = noOfIcIssued;
        this.rawMaterialAcceptedMt = rawMaterialAcceptedMt;
        this.rawMaterialRejectionPercentage = rawMaterialRejectionPercentage;
        this.processInspectionMaterialAcceptedNos = processInspectionMaterialAcceptedNos;
        this.processInspectionMaterialRejectionPercentage = processInspectionMaterialRejectionPercentage;
        this.finalInspectionMaterialAcceptedNos = finalInspectionMaterialAcceptedNos;
        this.finalInspectionMaterialRejectionPercentage = finalInspectionMaterialRejectionPercentage;
    }




}
