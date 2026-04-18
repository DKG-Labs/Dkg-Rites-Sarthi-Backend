package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AggregateGranulometricRequestDto {
    private Long requestId;
    private LocalDate testDate;
    private String typeOfTesting;
    private String consignmentNo;
    
    private List<AggregateGranulometricRowDto> observations;
    
    // Mix Proportions
    private Double mixCa1;
    private Double mixCa2;
    private Double mixFa;

    // Sample Weights
    private Double wtCa1;
    private Double wtCa2;
    private Double wtFa;

    // Session Context
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;

    private Integer createdBy;
}
