package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CementFinenessResponseDto {
    private Long id;
    private String typeOfTesting;
    private LocalDate testDate;
    private String consignmentNo;
    private Long requestId;
    private Double sampleWeightW1;
    private Double residueWeightW2;
    private Double residue1;
    private Double residue2;
    private Double residue3;
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
