package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.time.LocalDateTime;

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
    private Double totalRft;

    private String remarks;

    private Long createdBy;
    private LocalDateTime createdDate;

    private Long updatedBy;
    private LocalDateTime updatedDate;

  //  private List<ProductionStressChamberResponseDto> chambers;

 //   private List<ProductionLongLineGangResponseDto> gangs;

}
