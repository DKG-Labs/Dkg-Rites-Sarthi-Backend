package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CementFinenessRequestDto {
    private LocalDate testDate;
    private String typeOfTesting;
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
}
