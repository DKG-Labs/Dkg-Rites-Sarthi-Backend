package com.sarthi.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating multiple inventory entries with multiple heats against one
 * TC
 */
@Data
public class InventoryBulkEntryRequestDto {

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

    private String tcNumber;
    private String tcDate; // Format: yyyy-MM-dd
    private String unitOfMeasurement;
    private String tcFileBase64;

    private List<HeatDetailsDto> heatEntries;

    @Data
    public static class HeatDetailsDto {
        private String heatNumber;
        private BigDecimal tcQuantity; // Qty of that Heat in TC
        private Integer numberOfBundles;
        private String subPoNumber;
        private String subPoDate;
        private BigDecimal subPoQty;
        private String invoiceNumber;
        private String invoiceDate;
    }
}
