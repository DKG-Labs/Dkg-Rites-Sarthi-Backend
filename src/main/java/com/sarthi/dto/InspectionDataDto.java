package com.sarthi.dto;

import java.time.LocalDate;

public record InspectionDataDto(
    String icNumber,
    String poNo,
    String poSerialNo,
    String vendorId,
    String typeOfCall,
    LocalDate desiredInspectionDate,
    String placeOfInspection,
    String unitAddress,
    String companyName,
    java.time.LocalDateTime deliveryDate,
    java.time.LocalDateTime extendedDeliveryDate
) {}
