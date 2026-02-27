package com.sarthi.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TotalManufaturedQtyOfPoDto {

    private BigDecimal manufaturedQty;
    private BigDecimal rejectedQty;
    private BigDecimal rmAcceptedQty;
    private BigDecimal acceptedQty;
    private String heatNo;
    private BigDecimal weightAcceptedMt;
    private Integer offeredEarlier;
    private String sealingType;
    private String steelStampNumber;
    private String hologramDetails;

    public TotalManufaturedQtyOfPoDto(
            BigDecimal manufaturedQty,
            BigDecimal rejectedQty,
            BigDecimal rmAcceptedQty,
            BigDecimal acceptedQty,
            String heatNo,
            Integer offeredEarlier
           // BigDecimal weightAcceptedMt
    ) {
        this.manufaturedQty = manufaturedQty;
        this.rejectedQty = rejectedQty;
        this.rmAcceptedQty = rmAcceptedQty;
        this.acceptedQty = acceptedQty;
        this.heatNo = heatNo;
        this.offeredEarlier = offeredEarlier;
    }


    public TotalManufaturedQtyOfPoDto() {
    }

}
