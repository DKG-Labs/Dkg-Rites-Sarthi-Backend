package com.sarthi.dto;

import java.time.LocalDate;

public record InspectionDataDto(
        String icNumber,
        String poNo,
        String vendorId,
        String vendorName,
        String typeOfCall,
        LocalDate desiredInspectionDate,
        String placeOfInspection,
        String poSerialNo,
        LocalDate origDp,
        LocalDate extDp,
        String rlyShortName) {
}
