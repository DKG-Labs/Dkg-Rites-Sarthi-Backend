package com.sarthi.dto.reports;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PoInspection1stLevelStatusDto {

    private Integer slNo;
    private String railway;
    private String poNo;
    private LocalDateTime poDate;
    private String vendor;
    private String inspectionRegion;

    private Integer poQty;
    private Integer finalQuantityAcceptedByRites;
    private Integer balancePoQty;

    private Double rawMaterialRejectionPercentage;
    private Double processInspectionRejectionPercentage;
    private Double finalInspectionRejectionPercentage;

    private String poStatus;

    private String itemCatDescr;


    public PoInspection1stLevelStatusDto(
            Integer slNo,
            String railway,
            String poNo,
            LocalDateTime poDate,
            String vendor,
            String inspectionRegion,
            Long poQty,
            Integer finalQuantityAcceptedByRites,
            Integer balancePoQty,
            Double rawMaterialRejectionPercentage,
            Double processInspectionRejectionPercentage,
            Double finalInspectionRejectionPercentage,
            String poStatus,
            String itemCatDescr
    ) {
        this.slNo = slNo;
        this.railway = railway;
        this.poNo = poNo;
        this.poDate = poDate;
        this.vendor = vendor;
        this.inspectionRegion = inspectionRegion;
        this.poQty = Math.toIntExact(poQty);
        this.finalQuantityAcceptedByRites = finalQuantityAcceptedByRites;
        this.balancePoQty = balancePoQty;
        this.rawMaterialRejectionPercentage = rawMaterialRejectionPercentage;
        this.processInspectionRejectionPercentage = processInspectionRejectionPercentage;
        this.finalInspectionRejectionPercentage = finalInspectionRejectionPercentage;
        this.poStatus = poStatus;
        this.itemCatDescr =itemCatDescr;
    }
}
