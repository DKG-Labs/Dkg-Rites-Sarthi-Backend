package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

@Data
public class ProductionBenchRequestDto {

    private Long id;

    private String benchNumbers;   // example: 10A,10B,10C
    private Integer count;
    private String sleeperType;

    private Integer mouldPerBench;
    private Double rftMeters;
}
