package com.sarthi.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO for pre-inspection summary data.
 */
@Data
public class RmPreInspectionDataDto {

    private String inspectionCallNo;
    private Integer totalHeatsOffered;
    private BigDecimal totalQtyOfferedMt;
    private Integer numberOfBundles;
    private Integer numberOfErc;
    private String productModel;
    private Double divisor;
    private String poNo;
    private String poDate;
    private String vendorName;
    private String placeOfInspection;
    private String sourceOfRawMaterial;
}

