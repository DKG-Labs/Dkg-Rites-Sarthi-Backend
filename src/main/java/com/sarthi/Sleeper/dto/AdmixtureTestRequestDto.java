package com.sarthi.Sleeper.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AdmixtureTestRequestDto {
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
}
