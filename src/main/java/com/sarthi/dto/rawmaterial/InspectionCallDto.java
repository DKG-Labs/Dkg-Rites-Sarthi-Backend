package com.sarthi.dto.rawmaterial;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO for InspectionCall entity.
 * Used for API request/response for inspection call data - matches actual database schema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionCallDto {

    private Integer id;

    /* ==================== Call Information ==================== */

    private String icNumber;
    private String poNo;
    private String poSerialNo;
    private String typeOfCall;
    private String ercType;
    private String status;
    private String desiredInspectionDate;
    private String actualInspectionDate;
    private String placeOfInspection;
    

    /* ==================== Company Information ==================== */

    private Integer companyId;
    private String companyName;

    /* ==================== Unit Information ==================== */

    private Integer unitId;
    private String unitName;
    private String unitAddress;

    /* ==================== Additional Info ==================== */

    private String remarks;
    private String callCategory;
    private String callNature;
    private Boolean isDivertedMaterial;
    private String diversionRequestNo;
    private String sourceIcNo;

    /* ==================== Audit Fields ==================== */

    private String createdBy;
    private String updatedBy;
    private String createdAt;
    private String updatedAt;

    /* ==================== Nested DTOs ==================== */

    private RmInspectionDetailsDto rmInspectionDetails;
    private List<RmHeatQuantityDto> rmHeatQuantities;
}

