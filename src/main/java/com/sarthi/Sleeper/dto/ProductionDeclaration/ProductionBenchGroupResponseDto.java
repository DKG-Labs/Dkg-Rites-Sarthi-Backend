package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;
@Data
public class ProductionBenchGroupResponseDto {

    private Long id;

    private Integer benchNo;

    private String sleeperType;

    private Integer mouldPerBench;

    private Double rft;

    private String sleeperCategory;

    private Integer totalSleepers;

  //  private List<String> sleepers;
    private List<ProductionSleeperResponseDto> sleeperList;

}
