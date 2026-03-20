package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CementSpecificSurfaceRequestDto {
    private LocalDate testDate;
    private String typeOfTesting;
    private Long requestId;
    private String consignmentNo;
    private Double roomTemp;
    private Double weight;
    private Double standardTimeTs;
    private Double standardSurfaceFs;
    private Double sampleTime1;
    private Double sampleTime2;
    private Double sampleTime3;
    private Double avgTime;
    private Double specificSurfaceFm;
    private String result;
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;
    private Integer createdBy;
}
