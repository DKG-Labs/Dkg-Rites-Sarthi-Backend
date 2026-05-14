package com.sarthi.SRailPad.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * Request DTO for saving Section A & B verification data
 * along with shift details from the "OPEN & VERIFY FORM" popup.
 */
@Data
public class RailInitiationVerificationDto {

    private String callNo;

    // ---- Section A ----
    private String rlyPoNo;
    private String poNo;
    private String poDate;
    private Integer poQty;
    private Integer poSrQty;
    private String vendorName;
    private String vendorCode;
    private String maNo;
    private String maDate;
    private String purchasingAuthority;
    private String billPayingOfficer;
    private String sectionAStatus;  // "approved" | "rejected"

    // ---- Section B ----
    private String rlyPoNoSerial;
    private String itemDesc;
    private String ercType;
    private String unit;
    private String consignee;
    private String origDp;
    private String extDp;
    private String callQty;
    private String qtyUnit;
    private String placeOfInspection;
    private String remarks;
    private String sectionBStatus;  // "approved" | "rejected"

    // ---- Shift Details ----
    private String shift;
    private String company;
    private LocalDate castingDate;
    private String productionUnit;

    // ---- Audit ----
    private Long verifiedBy;
}
