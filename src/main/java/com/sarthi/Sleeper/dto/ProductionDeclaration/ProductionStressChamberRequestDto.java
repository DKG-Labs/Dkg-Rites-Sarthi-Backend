package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionStressChamberRequestDto {

    private Long id;

    private Integer chamberNo;

    private String lbcTime;

    private List<ProductionBenchGroupRequestDto> benchGroups;

}