package com.sarthi.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for creating/updating inventory entries
 */
@Data
public class InventoryEntryRequestDto {

    private String vendorCode;
    private String vendorName;
    
    private Long companyId;
    private String companyName;
    
    private String supplierName;
    private String unitName;
    private String supplierAddress;
    
    private String rawMaterial;
    private String gradeSpecification;
    private BigDecimal lengthOfBars;
    
    private String heatNumber;
    private String tcNumber;
    private String tcDate;  // Format: yyyy-MM-dd
    private BigDecimal tcQuantity;
    
    private String subPoNumber;
    private String subPoDate;  // Format: yyyy-MM-dd
    private BigDecimal subPoQty;
    
    private String invoiceNumber;
    private String invoiceDate;  // Format: yyyy-MM-dd
    
    private String unitOfMeasurement;
    private BigDecimal rateOfMaterial;
    private BigDecimal rateOfGst;
    private BigDecimal baseValuePo;
    private BigDecimal totalPo;
    private Integer numberOfBundles;
}

