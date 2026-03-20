package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CementNormalConsistencyRequestDto {
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
    private List<CementNormalConsistencyObservationDto> observations;
}
