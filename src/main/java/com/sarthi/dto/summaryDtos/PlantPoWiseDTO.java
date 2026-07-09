package com.sarthi.dto.summaryDtos;

import lombok.Data;

@Data
public class PlantPoWiseDTO {

    private String plantName;

    private Long noOfPos;

    private Double poQty;

    private Double rawMaterialAccepted;

    private Double processInspectionAcceptance;

    private Double finalAcceptance;

    private Double totalFinalAccepted;

    private Double balance;
}