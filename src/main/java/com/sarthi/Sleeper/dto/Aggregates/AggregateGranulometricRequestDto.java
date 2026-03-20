package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AggregateGranulometricRequestDto {
    private LocalDate testDate;
    private Long requestId;
    private String consignmentNo;
    
    private List<AggregateGranulometricRowDto> observations;

    // Session Context
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;

    private Integer createdBy;
}
