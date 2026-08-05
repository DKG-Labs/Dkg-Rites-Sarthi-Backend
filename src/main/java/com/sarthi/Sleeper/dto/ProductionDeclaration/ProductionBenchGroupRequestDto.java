package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;

@Data
public class ProductionBenchGroupRequestDto {

    private Long id;

    private String benchNo;

    private String sleeperType;

    private Integer mouldPerBench;

    private Double rft;

    private String sleeperCategory;

    private Integer totalSleepers;

    private List<String> sleepers;
    private List<ProductionSleeperResponseDto> sleeperList;

}
