package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AggregateSoundnessResponseDto {
    private Long id;
    private Long requestId;
    private LocalDate testDate;
    private String typeOfTesting;
    private String consignmentNo;
    private String method;
    private Integer cycles;
    private Double initialWt;
    private Double finalWt;
    private Double lossWt;
    private Double lossPercent;
    private String result;

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
