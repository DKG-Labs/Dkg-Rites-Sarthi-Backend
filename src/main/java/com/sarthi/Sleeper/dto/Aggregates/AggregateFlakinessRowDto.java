package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;

@Data
public class AggregateFlakinessRowDto {
    private String category;
    private Double passingSize;
    private Double retainedSize;
    private Double weightSampleA;
    private Double weightPassedB;
    private Double weightRetainedC;
    private Double weightRetainedLengthD;
}
