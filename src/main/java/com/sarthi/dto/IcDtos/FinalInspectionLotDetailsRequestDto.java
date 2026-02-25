package com.sarthi.dto.IcDtos;

import lombok.Data;

/**
 * DTO for Final Inspection Lot Details Request
 * Contains individual lot information for Final inspection
 */
@Data
public class FinalInspectionLotDetailsRequestDto {

    // ---- LOT INFORMATION ----
    private String lotNumber;

    // ---- HEAT INFORMATION ----
    private String heatNumber;
    private String manufacturer;
    private String manufacturerHeat;

    // ---- QUANTITY INFORMATION ----
    private Integer offeredQty;

    // ---- PROCESS IC REFERENCE ----
    private String processIcNumber;

    // ---- ADDITIONAL INFORMATION ----
    private Integer noOfBags;
}
