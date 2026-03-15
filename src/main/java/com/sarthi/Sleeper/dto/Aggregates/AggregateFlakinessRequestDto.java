package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AggregateFlakinessRequestDto {
    private LocalDate testDate;
    private String consignmentNo;
    private Double combinedIndex20mm;
    private String result20mm;
    private Double combinedIndex10mm;
    private String result10mm;
    
    private List<AggregateFlakinessRowDto> observations;

    // Session Context
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;

    private Integer createdBy;
}
