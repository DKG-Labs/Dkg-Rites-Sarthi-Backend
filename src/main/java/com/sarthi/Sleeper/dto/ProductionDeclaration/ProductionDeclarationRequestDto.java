package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionDeclarationRequestDto {

    private String plantType;
    private String productionUnit;

    private String castingDate;
    private String shift;

    private String batchNumber;
    private String mixDesignReference;

    private String lbcTime;

    private Integer totalCastedSleepers;
    private Integer totalSleeperTypes;
    private Double totalRft;

    private String remarks;

    private Long createdBy;
    private Long updatedBy;

    private List<ProductionStressChamberRequestDto> chambers;

    private List<ProductionLongLineGangRequestDto> gangs;

}