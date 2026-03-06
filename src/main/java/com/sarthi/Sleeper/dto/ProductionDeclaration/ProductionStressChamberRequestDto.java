package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionStressChamberRequestDto {

    private Integer chamberNo;

    private List<ProductionBenchGroupRequestDto> benchGroups;

}