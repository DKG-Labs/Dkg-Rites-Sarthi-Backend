package com.sarthi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for inventory entry response
 */
@Data
public class InventoryEntryResponseDto {

    private Long id;

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
    private LocalDate tcDate;
    private BigDecimal tcQuantity;

    private BigDecimal offeredQuantity;
    private BigDecimal qtyLeftForInspection;

    private String subPoNumber;
    private LocalDate subPoDate;
    private BigDecimal subPoQty;

    private String invoiceNumber;
    private LocalDate invoiceDate;

    private String unitOfMeasurement;
    private BigDecimal rateOfMaterial;
    private BigDecimal rateOfGst;
    private BigDecimal baseValuePo;
    private BigDecimal totalPo;

    private String status;
    private Integer numberOfBundles;
    private String tcFilePath;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
