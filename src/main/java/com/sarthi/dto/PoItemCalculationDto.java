package com.sarthi.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * Dedicated DTO for returning PO Sr No QTY, PO Sr No VALUE, Derived Rate,
 * Offered Qty, Material Value, and PO Category for Inspection Call calculations.
 */
@Data
public class PoItemCalculationDto {
    private String callNo;
    private String rawPoNo;
    private String barePoNo;
    private String itemSrNo;
    private Integer poQty;
    private BigDecimal poValue;
    private BigDecimal rate;
    private Double offeredQty;
    private BigDecimal materialValue;
    private String poDate;
    private String poCategory; // 'A' | 'B' | 'C'
    private String vendorName;
    private String rlyShortName;
}
