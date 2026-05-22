package com.sarthi.dto.vendorDtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VendorPoHeaderResponseDto {

    private String poNo;
    private String poDate;
    private String poDes;
    private BigDecimal qty;
    private String unit;
    private String rlyShortName;
    private String rlyCd;

    private String itemCategory;
    private BigDecimal totalValue;
    private String status;
    private String pdfPath;

    private List<VendorPoItemsResponseDto> poItem;

}
