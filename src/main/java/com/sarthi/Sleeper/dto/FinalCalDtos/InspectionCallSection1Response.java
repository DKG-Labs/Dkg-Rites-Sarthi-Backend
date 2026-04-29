package com.sarthi.Sleeper.dto.FinalCalDtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionCallSection1Response {
    private String rlyPoNo;
    private LocalDateTime poDate;
    private Integer poQty;
    private String vendorName;
    private String maNo;
    private String maDate;
    private String purchasingAuthority;
    private String billPayingOfficer;
}
