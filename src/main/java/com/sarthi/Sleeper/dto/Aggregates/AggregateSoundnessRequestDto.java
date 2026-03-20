package com.sarthi.Sleeper.dto.Aggregates;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AggregateSoundnessRequestDto {
    private LocalDate testDate;
    private Long requestId;
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

    private Integer createdBy;
}
