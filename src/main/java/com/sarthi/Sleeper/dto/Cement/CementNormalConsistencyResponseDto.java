package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CementNormalConsistencyResponseDto {
    private Long id;
    private LocalDate testDate;
    private String typeOfTesting;
    private Long requestId;
    private String consignmentNo;
    private Double roomTemp;
    private Double sampleWeight;
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
    private List<CementNormalConsistencyObservationDto> observations;
}
