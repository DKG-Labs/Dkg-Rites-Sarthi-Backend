package com.sarthi.dto.reports;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InspectionCallsReportDto {

    private String callNumber;

    private String productAndStageOfInspection;

    private String poNumber;

    private LocalDateTime deliveryDate;

    private  LocalDateTime expectedDeliveryDate;

    private String vendorName;

    private LocalDate inspectionDesiredDate;

    private LocalDateTime callDate;

    private String ieName;

    private String cmName;

    private String ritesRio;

    private String status;
}