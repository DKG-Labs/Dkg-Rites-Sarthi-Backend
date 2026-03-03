package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionDeclarationResponseDto {

    private Long id;

    private String plantType;
    private String productionUnit;
    private String castingDate;
    private String shift;

    private String batchNumber;
    private String mixDesignReference;
    private String lbcTime;

    private Integer totalCastedSleepers;
    private Integer totalSleeperTypes;
    private Double totalRftCasted;

    private String remarks;

    private List<ProductionChamberResponseDto> chambers;
}
