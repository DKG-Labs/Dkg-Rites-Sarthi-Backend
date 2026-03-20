package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Cement7DayStrengthResponseDto {
    private Long id;
    private String typeOfTesting;
    private Long requestId;
    private String consignmentNo;
    private LocalDate testDate;
    private Double roomTemp;
    private Double normalConsistency;
    private Double waterRequired;
    private Double minStrength;
    private String cubeResult;
    private Double soundness;
    private String soundnessResult;
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;
    private List<Cement7DayStrengthCubeDto> cubes;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
}
