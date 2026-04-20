package com.sarthi.Sleeper.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WaterQualityTestDto {
    private Long id;
    private LocalDate testDate;
    private Double phValue;
    private Double tdsResult;
    private String result;
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
}
