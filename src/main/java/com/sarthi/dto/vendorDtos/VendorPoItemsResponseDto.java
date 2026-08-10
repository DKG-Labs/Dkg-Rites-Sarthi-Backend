package com.sarthi.dto.vendorDtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VendorPoItemsResponseDto {

    private String poSerialNo;
    private String poDes;
    private String conigness;
    private BigDecimal orderedQty;
    private String deliveryPeriod;
    private String extendedDeliveryPeriod;

    private String itemSrNo;
    private BigDecimal offeredTillNow;
    private BigDecimal acceptedTillNow;
    private BigDecimal due;
    private String uom;
    private String unit;
}
