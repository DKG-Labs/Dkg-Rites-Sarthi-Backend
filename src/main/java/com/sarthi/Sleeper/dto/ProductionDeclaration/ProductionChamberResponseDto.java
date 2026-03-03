package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionChamberResponseDto {

    private Long id;
    private String chamberNo;

    private List<ProductionBenchResponseDto> benches;
}