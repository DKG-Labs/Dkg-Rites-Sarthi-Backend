package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AggregateGranulometricResponseDto {
    private Long id;
    private LocalDate testDate;
    private Long requestId;
    private String consignmentNo;
    
    private List<AggregateGranulometricRowDto> observations;

    // Session Context
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;

    // Audit Fields
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
}
