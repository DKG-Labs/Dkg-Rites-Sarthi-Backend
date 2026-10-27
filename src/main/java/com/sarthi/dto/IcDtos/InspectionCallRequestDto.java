package com.sarthi.dto.IcDtos;

import lombok.Data;

@Data
public class InspectionCallRequestDto {

    private String icNumber;
    private String poNo;
    private String poSerialNo;
    private String typeOfCall;
    private String ercType;
    private String status;

    private String placeOfInspection;
    private String vendorId;

    // ---- DATES (yyyy-MM-dd) ----
    private String desiredInspectionDate;
    private String actualInspectionDate;

    // ---- COMPANY / UNIT ----
    private Integer companyId;
    private String companyName;
    private Integer unitId;
    private String unitName;
    private String unitAddress;

    // ---- MISC ----
    private String remarks;
    private String createdBy;
    private String updatedBy;

    // ---- CALL CATEGORY / DIVERSION / NATURE ----
    private String callCategory;
    private String callNature;
    private Boolean isDivertedMaterial;
    private String diversionRequestNo;
    private String sourceIcNo;
}
