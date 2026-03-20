package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CementFinenessResponseDto {
    private Long id;
    private LocalDate testDate;
    private String consignmentNo;
    private Long requestId;
    private Double sampleWeightW1;
    private Double residueWeightW2;
    private Double percentageFineness;
    private String result;
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
}
