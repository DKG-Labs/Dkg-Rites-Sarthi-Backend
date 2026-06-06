package com.sarthi.SRailPad.dto.ieVerification;

import lombok.Data;
import java.time.LocalDate;

import java.util.List;

@Data
public class RailFinalInspectionLotResultsRequestDto {
    private String callNo;
    private String shift;
    private LocalDate dateOfInspection;
    private String plantId;
    private String rlyPoSrNo;
    private String vendorName;
    private String vendorCode;
    private String railpadType;
    private String lotNo;
    private Integer offeredQty;
    private Integer acceptedQty;
    private Integer rejectedQty;
    private String visualDimensionalStatus;
    private String physicalAgeingPropertiesStatus;
    private String electricalChemicalStatus;
    private String dynamicDurabilityTestStatus;
    private String ncrgrspStatus;
    private String overallStatus;
    private String hologram;
    private String remarks;
    private Long userId;
    private List<RailFinalInspectionSectionResultDto> sectionResults;
}
