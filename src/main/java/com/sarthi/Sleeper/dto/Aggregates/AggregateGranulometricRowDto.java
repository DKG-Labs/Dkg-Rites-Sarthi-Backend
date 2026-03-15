package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;

@Data
public class AggregateGranulometricRowDto {
    private String sectionType;
    private String sieveSize;
    private Double wtRetained;
    private Double cummWtRetained;
    private Double pctRetained;
    private Double pctPassing;
}
