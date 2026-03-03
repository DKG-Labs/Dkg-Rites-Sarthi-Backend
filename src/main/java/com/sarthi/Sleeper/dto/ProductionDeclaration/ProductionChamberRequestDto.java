package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionChamberRequestDto {

    private Long id;
    private String chamberNo;

    private List<ProductionBenchRequestDto> benches;
}
