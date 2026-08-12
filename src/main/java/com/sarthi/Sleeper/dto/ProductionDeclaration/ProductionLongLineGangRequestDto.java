package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionLongLineGangRequestDto {

    private Long id;
    private String mode;

    private String gangFrom;
    private String gangTo;

    private String gangNo;

    private String sleeperType;

    private Integer mouldsPerGang;

    private String sleeperCategory;

    private Integer totalSleepers;

    private Double rft;
    private String lbcTime;

     private List<String> sleepers;
    private List<ProductionSleeperResponseDto> sleeperList;
}
