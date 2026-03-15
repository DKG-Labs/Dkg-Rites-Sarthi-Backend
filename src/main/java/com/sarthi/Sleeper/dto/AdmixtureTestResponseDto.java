package com.sarthi.Sleeper.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdmixtureTestResponseDto {
    private Long id;
    private LocalDate testDate;
    private String consignmentNo;
    private Double dosage;
    private Double density;
    private Double ph;
    private String result;

    private String shift;
    private String lineNo;
    private LocalDate dateOfInspection;

    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer updatedBy;
    private LocalDateTime updatedDate;
}
