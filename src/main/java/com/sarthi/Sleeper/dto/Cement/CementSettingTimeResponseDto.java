package com.sarthi.Sleeper.dto.Cement;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class CementSettingTimeResponseDto {
    private Long id;
    private LocalDate testDate;
    private String typeOfTesting;
    private String consignmentNo;
    private Double roomTemp;
    private Double weight;
    private Double normalConsistency;
    private Double waterAdded;
    private LocalTime timeOfAddingWater;
    private LocalTime mouldReadyAt;
    private Integer initialSettingTime;
    private Integer finalSettingTime;
    private String result;
    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
    private List<CementSettingTimeObservationDto> observations;
}
