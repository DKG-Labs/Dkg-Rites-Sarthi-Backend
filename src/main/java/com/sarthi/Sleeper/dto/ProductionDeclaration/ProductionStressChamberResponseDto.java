package com.sarthi.Sleeper.dto.ProductionDeclaration;

import lombok.Data;

import java.util.List;
@Data
public class ProductionStressChamberResponseDto {

    private Long id;

    private Integer chamberNo;

    private java.time.LocalTime lbcTime;

    private List<ProductionBenchGroupResponseDto> benchGroups;

}
