package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionCallSection1Response {
    private String rlyPoNo;
    private LocalDateTime poDate;
    private java.math.BigDecimal poQty;
    private String uom;
    private String vendorName;
    private String maNo;
    private String maDate;
    private String purchasingAuthority;
    private String billPayingOfficer;
}
