package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionLongLineGangResponseDto {

    private Long id;

    private String mode;

    private Integer gangFrom;
    private Integer gangTo;

    private Integer gangNo;

    private String sleeperType;

    private Integer mouldsPerGang;

    private String sleeperCategory;

    private Integer totalSleepers;

    private Double rft;

   // private List<String> sleepers;
   private List<ProductionSleeperResponseDto> sleeperList;

}
