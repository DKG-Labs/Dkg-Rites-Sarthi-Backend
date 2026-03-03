package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

@Data
public class ProductionBenchResponseDto {

    private Long id;

    private String benchNumbers;
    private Integer count;
    private String sleeperType;

    private Integer mouldPerBench;
    private Double rftMeters;
}
