package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SectionARequest {

    private String callNo;

    private String rlyPoNo;
    private LocalDateTime poDate;

    private java.math.BigDecimal poQty;
    private String vendorName;

    private String maNo;
    private LocalDate maDate;

    private String purchasingAuthority;
    private String billPayingOfficer;

    private String plantId;
    private String vendorCode;
    private String shift;

    private Long createdBy;
}