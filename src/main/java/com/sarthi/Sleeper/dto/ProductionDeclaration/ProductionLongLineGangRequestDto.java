package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionLongLineGangRequestDto {
    private String mode;

    private Integer gangFrom;
    private Integer gangTo;

    private Integer gangNo;

    private String sleeperType;

    private Integer mouldsPerGang;

}
