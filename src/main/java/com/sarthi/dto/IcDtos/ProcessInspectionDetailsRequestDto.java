package com.sarthi.dto.IcDtos;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProcessInspectionDetailsRequestDto {

    // ---- RM IC REFERENCE ----
    private String rmIcNumber;

    // ---- LOT INFORMATION ----
    private String lotNumber;

    // ---- HEAT INFORMATION ----
    private String heatNumber;
    private String manufacturer;
    private String manufacturerHeat;

    // ---- QUANTITY INFORMATION ----
    private Integer offeredQty;
    private Integer totalAcceptedQtyRm;
    private Integer declaredLotSize;

    // ---- PRODUCTION INFORMATION ----
    private LocalDate tentativeStartDate;

    // ---- PLACE OF INSPECTION ----
    private Integer companyId;
    private String companyName;
    private Integer unitId;
    private String unitName;
    private String unitAddress;
}

